package com.liftkart.notification.controller;

import com.liftkart.notification.dto.response.ApiResponse;
import com.liftkart.notification.dto.response.NotificationResponse;
import com.liftkart.notification.dto.response.UnreadCountResponse;
import com.liftkart.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications for current user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched",
                notificationService.getNotifications(userId, pageable)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @RequestHeader("X-User-Id") UUID userId) {

        return ResponseEntity.ok(ApiResponse.success("Unread count fetched",
                notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID notificationId,
            @RequestHeader("X-User-Id") UUID userId) {

        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(
                ApiResponse.success("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader("X-User-Id") UUID userId) {

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID notificationId) {

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(
                ApiResponse.success("Notification deleted", null));
    }
}