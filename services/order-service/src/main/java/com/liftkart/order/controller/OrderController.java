package com.liftkart.order.controller;

import com.liftkart.order.dto.request.CreateAddressRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.liftkart.order.dto.request.PlaceOrderRequest;
import com.liftkart.order.dto.request.UpdateOrderStatusRequest;
import com.liftkart.order.dto.response.*;
import com.liftkart.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestHeader("X-User-Id") UUID customerId,
            @Valid @RequestBody PlaceOrderRequest request) {

        // Convert nested items to Map format expected by service
        List<Map<String, Object>> cartItems = request.getItems().stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", item.getProductId().toString());
                    map.put("productName", item.getProductName());
                    map.put("imageUrl", item.getImageUrl());
                    map.put("price", item.getPrice().toString());
                    map.put("quantity", item.getQuantity().toString());
                    map.put("vendorId", item.getVendorId() != null
                            ? item.getVendorId().toString()
                            : UUID.randomUUID().toString());
                    return map;
                })
                .collect(Collectors.toList());

        OrderResponse response = orderService.placeOrder(
                customerId, request, cartItems);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all orders for current customer")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Orders fetched",
                orderService.getCustomerOrders(customerId, pageable)));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @RequestHeader("X-User-Id") UUID customerId,
            @PathVariable UUID orderId) {

        return ResponseEntity.ok(ApiResponse.success("Order fetched",
                orderService.getOrderById(orderId, customerId)));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status (Admin/Vendor)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader(value = "X-User-Role",
                    defaultValue = "ADMIN") String role) {

        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(orderId, request, userId, role)));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @RequestHeader("X-User-Id") UUID customerId,
            @PathVariable UUID orderId) {

        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(orderId, customerId)));
    }
}