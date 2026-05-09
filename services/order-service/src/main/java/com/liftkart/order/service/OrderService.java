package com.liftkart.order.service;

import com.liftkart.order.dto.request.PlaceOrderRequest;
import com.liftkart.order.dto.request.UpdateOrderStatusRequest;
import com.liftkart.order.dto.response.*;
import com.liftkart.order.entity.*;
import com.liftkart.order.exception.BadRequestException;
import com.liftkart.order.exception.ResourceNotFoundException;
import com.liftkart.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.rabbitmq.exchanges.order}")
    private String orderExchange;

    @Value("${app.rabbitmq.routing-keys.order-notification}")
    private String notificationRoutingKey;

    @Value("${app.rabbitmq.routing-keys.order-inventory}")
    private String inventoryRoutingKey;

    @Value("${app.kafka.topics.order-events}")
    private String orderEventsTopic;

    // ── Place Order ────────────────────────────────────────────────
    @Transactional
    public OrderResponse placeOrder(UUID customerId,
                                    PlaceOrderRequest request,
                                    List<Map<String, Object>> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BadRequestException("Cannot place order with empty cart");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found"));

        if (!address.getCustomerId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        // Build address snapshot
        Map<String, Object> addressSnapshot = new HashMap<>();
        addressSnapshot.put("fullName", address.getFullName());
        addressSnapshot.put("phone", address.getPhone());
        addressSnapshot.put("line1", address.getLine1());
        addressSnapshot.put("line2", address.getLine2());
        addressSnapshot.put("city", address.getCity());
        addressSnapshot.put("state", address.getState());
        addressSnapshot.put("pincode", address.getPincode());

        // Calculate totals
        BigDecimal subtotal = cartItems.stream()
                .map(item -> {
                    BigDecimal price = new BigDecimal(item.get("price").toString());
                    Integer qty = Integer.parseInt(item.get("quantity").toString());
                    return price.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = subtotal;

        // Create order
        Order order = Order.builder()
                .customerId(customerId)
                .addressSnapshot(addressSnapshot)
                .paymentMethod(request.getPaymentMethod())
                .subtotal(subtotal)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .build();

        order.setCreatedBy(customerId);
        order = orderRepository.save(order);

        // Create order items from cart
        final Order savedOrder = order;
        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> {
                    BigDecimal price = new BigDecimal(
                            item.get("price").toString());
                    Integer qty = Integer.parseInt(
                            item.get("quantity").toString());

                    OrderItem orderItem = OrderItem.builder()
                            .order(savedOrder)
                            .productId(UUID.fromString(
                                    item.get("productId").toString()))
                            .vendorId(UUID.fromString(
                                    item.get("vendorId") != null
                                            ? item.get("vendorId").toString()
                                            : UUID.randomUUID().toString()))
                            .productName(item.get("productName").toString())
                            .productImageUrl(item.get("imageUrl") != null
                                    ? item.get("imageUrl").toString() : null)
                            .quantity(qty)
                            .unitPrice(price)
                            .totalPrice(price.multiply(
                                    BigDecimal.valueOf(qty)))
                            .build();
                    orderItem.setCreatedBy(customerId);
                    return orderItem;
                })
                .collect(Collectors.toList());

        savedOrder.setItems(orderItems);

        // Record initial status
        addStatusHistory(savedOrder, null, "PENDING", customerId, "SYSTEM",
                "Order placed");

        // Simulate payment
        processPayment(savedOrder, request.getPaymentMethod(), totalAmount,
                customerId);

        order = orderRepository.save(savedOrder);

        // Publish events
        publishOrderEvents(order, cartItems);

        log.info("Order placed: {} for customer: {}", order.getId(), customerId);
        return mapToResponse(order);
    }

    // ── Get Customer Orders ────────────────────────────────────────
    public Page<OrderResponse> getCustomerOrders(UUID customerId,
                                                 Pageable pageable) {
        return orderRepository
                .findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        customerId, pageable)
                .map(this::mapToResponse);
    }

    // ── Get Order by ID ────────────────────────────────────────────
    public OrderResponse getOrderById(UUID orderId, UUID customerId) {
        Order order = orderRepository
                .findByIdAndCustomerIdAndIsDeletedFalse(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found"));
        return mapToResponse(order);
    }

    // ── Update Order Status (Admin/Vendor) ─────────────────────────
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId,
                                           UpdateOrderStatusRequest request,
                                           UUID userId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found"));

        String oldStatus = order.getStatus();
        order.setStatus(request.getStatus());
        addStatusHistory(order, oldStatus, request.getStatus(),
                userId, role, request.getComment());

        order = orderRepository.save(order);
        log.info("Order {} status updated: {} → {}",
                orderId, oldStatus, request.getStatus());
        return mapToResponse(order);
    }

    // ── Cancel Order ───────────────────────────────────────────────
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository
                .findByIdAndCustomerIdAndIsDeletedFalse(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found"));

        if (!order.getStatus().equals("PENDING") &&
                !order.getStatus().equals("CONFIRMED")) {
            throw new BadRequestException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");
        addStatusHistory(order, oldStatus, "CANCELLED",
                customerId, "CUSTOMER", "Cancelled by customer");

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    // ── Address Management ─────────────────────────────────────────
    @Transactional
    public AddressResponse createAddress(UUID customerId,
                                         com.liftkart.order.dto.request.CreateAddressRequest request) {

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository
                    .findByCustomerIdAndIsDefaultTrueAndIsDeletedFalse(
                            customerId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .customerId(customerId)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .isDefault(request.getIsDefault())
                .build();

        address.setCreatedBy(customerId);
        address = addressRepository.save(address);
        return mapAddressToResponse(address);
    }

    public List<AddressResponse> getAddresses(UUID customerId) {
        return addressRepository
                .findByCustomerIdAndIsDeletedFalse(customerId)
                .stream()
                .map(this::mapAddressToResponse)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ────────────────────────────────────────────
    private void processPayment(Order order, String method,
                                BigDecimal amount, UUID customerId) {
        // Simulate payment processing
        String refId = "LK-" + System.currentTimeMillis();

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(method.equals("COD") ? "PENDING" : "PAID")
                .amount(amount)
                .simulatedRefId(refId)
                .processedAt(LocalDateTime.now())
                .build();
        payment.setCreatedBy(customerId);

        paymentRepository.save(payment);

        order.setPaymentStatus(
                method.equals("COD") ? "PENDING" : "PAID");
        order.setStatus("CONFIRMED");

        addStatusHistory(order, "PENDING", "CONFIRMED",
                customerId, "SYSTEM", "Payment processed: " + refId);
    }

    private void addStatusHistory(Order order, String oldStatus,
                                  String newStatus, UUID changedBy,
                                  String role, String comment) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedByRole(role)
                .comment(comment)
                .build();
        history.setCreatedBy(changedBy);
        order.getStatusHistory().add(history);
    }

    private void publishOrderEvents(Order order,
                                    List<Map<String, Object>> items) {
        try {
            // RabbitMQ → Notification Service
            Map<String, Object> notificationEvent = new HashMap<>();
            notificationEvent.put("orderId", order.getId());
            notificationEvent.put("customerId", order.getCustomerId());
            notificationEvent.put("status", order.getStatus());
            notificationEvent.put("totalAmount", order.getTotalAmount());

            rabbitTemplate.convertAndSend(orderExchange,
                    notificationRoutingKey, notificationEvent);

            // RabbitMQ → Product Service (deduct inventory)
            items.forEach(item -> {
                Map<String, Object> inventoryEvent = new HashMap<>();
                inventoryEvent.put("productId", item.get("productId"));
                inventoryEvent.put("quantity", item.get("quantity"));
                inventoryEvent.put("orderId", order.getId());

                rabbitTemplate.convertAndSend(orderExchange,
                        inventoryRoutingKey, inventoryEvent);
            });

            // Kafka → Analytics
            Map<String, Object> kafkaEvent = new HashMap<>();
            kafkaEvent.put("orderId", order.getId());
            kafkaEvent.put("customerId", order.getCustomerId());
            kafkaEvent.put("totalAmount", order.getTotalAmount());
            kafkaEvent.put("eventType", "ORDER_PLACED");
            kafkaEvent.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send(orderEventsTopic,
                    order.getId().toString(), kafkaEvent);

        } catch (Exception e) {
            log.warn("Failed to publish order events: {}", e.getMessage());
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                  .filter(i -> !i.getIsDeleted())
                  .map(item -> OrderItemResponse.builder()
                               .id(item.getId())
                               .productId(item.getProductId())
                               .vendorId(item.getVendorId())
                               .productName(item.getProductName())
                               .productImageUrl(item.getProductImageUrl())
                               .quantity(item.getQuantity())
                               .unitPrice(item.getUnitPrice())
                               .totalPrice(item.getTotalPrice())
                               .status(item.getStatus())
                               .build())
                  .collect(Collectors.toList());

        List<OrderStatusHistoryResponse> historyResponses =
                order.getStatusHistory() == null
                        ? List.of()
                        : order.getStatusHistory().stream()
                          .map(h -> OrderStatusHistoryResponse.builder()
                                    .oldStatus(h.getOldStatus())
                                    .newStatus(h.getNewStatus())
                                    .changedByRole(h.getChangedByRole())
                                    .comment(h.getComment())
                                    .changedAt(h.getCreatedAt())
                                    .build())
                          .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .addressSnapshot(order.getAddressSnapshot())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .deliveryCharge(order.getDeliveryCharge())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .items(itemResponses)
                .statusHistory(historyResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private AddressResponse mapAddressToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .isDefault(address.getIsDefault())
                .build();
    }
}