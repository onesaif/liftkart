package com.liftkart.notification.consumer;

import com.liftkart.notification.entity.Notification;
import com.liftkart.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    // ── Listen for Order Events ────────────────────────────────────
    @RabbitListener(queues = "${app.rabbitmq.queues.order-notification}")
    public void handleOrderNotification(Map<String, Object> event) {
        try {
            log.info("Received order notification event: {}", event);

            UUID customerId = UUID.fromString(
                    event.get("customerId").toString());
            UUID orderId = UUID.fromString(
                    event.get("orderId").toString());
            String status = event.get("status").toString();
            String totalAmount = event.get("totalAmount").toString();

            String title = getOrderTitle(status);
            String message = getOrderMessage(status, orderId, totalAmount);

            Notification notification = Notification.builder()
                    .userId(customerId)
                    .title(title)
                    .message(message)
                    .type("ORDER_UPDATE")
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .build();

            notification.setCreatedBy(customerId);
            notificationRepository.save(notification);

            log.info("Order notification saved for customer: {}", customerId);

        } catch (Exception e) {
            log.error("Failed to process order notification: {}", e.getMessage());
        }
    }

    // ── Listen for Vendor Approved Events ─────────────────────────
    @RabbitListener(queues = "${app.rabbitmq.queues.vendor-approved}")
    public void handleVendorApproved(Map<String, Object> event) {
        try {
            log.info("Received vendor approved event: {}", event);

            UUID userId = UUID.fromString(event.get("userId").toString());
            String storeName = event.getOrDefault(
                    "storeName", "Your store").toString();

            Notification notification = Notification.builder()
                    .userId(userId)
                    .title("Store Approved! 🎉")
                    .message("Congratulations! Your store '" + storeName +
                            "' has been approved. You can now list products.")
                    .type("VENDOR_APPROVED")
                    .build();

            notification.setCreatedBy(userId);
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to process vendor approved event: {}",
                    e.getMessage());
        }
    }

    // ── Listen for Low Stock Alerts ────────────────────────────────
    @RabbitListener(queues = "${app.rabbitmq.queues.low-stock}")
    public void handleLowStock(String message) {
        try {
            log.info("Received low stock alert: {}", message);
            // In a real system, extract vendor ID and product ID from message
            // For now just log it — the message format is a plain string
            log.warn("Low stock alert received: {}", message);

        } catch (Exception e) {
            log.error("Failed to process low stock alert: {}", e.getMessage());
        }
    }

    // ── Private Helpers ────────────────────────────────────────────
    private String getOrderTitle(String status) {
        return switch (status) {
            case "CONFIRMED"  -> "Order Confirmed ✅";
            case "PROCESSING" -> "Order is Being Processed 📦";
            case "SHIPPED"    -> "Order Shipped 🚚";
            case "DELIVERED"  -> "Order Delivered 🎉";
            case "CANCELLED"  -> "Order Cancelled ❌";
            default           -> "Order Update";
        };
    }

    private String getOrderMessage(String status, UUID orderId,
                                   String totalAmount) {
        String shortId = orderId.toString().substring(0, 8).toUpperCase();
        return switch (status) {
            case "CONFIRMED"  -> "Your order #" + shortId +
                    " worth ₹" + totalAmount +
                    " has been confirmed. Payment received!";
            case "SHIPPED"    -> "Your order #" + shortId +
                    " is on its way. Track it in your orders.";
            case "DELIVERED"  -> "Your order #" + shortId +
                    " has been delivered. Enjoy your purchase!";
            case "CANCELLED"  -> "Your order #" + shortId +
                    " has been cancelled.";
            default           -> "Your order #" + shortId +
                    " status updated to: " + status;
        };
    }
}