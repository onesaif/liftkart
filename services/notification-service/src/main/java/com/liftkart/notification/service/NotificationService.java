package com.liftkart.notification.service;

import com.liftkart.notification.dto.response.NotificationResponse;
import com.liftkart.notification.dto.response.UnreadCountResponse;
import com.liftkart.notification.entity.Notification;
import com.liftkart.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationResponse> getNotifications(
            UUID userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        userId, pageable)
                .map(this::mapToResponse);
    }

    public UnreadCountResponse getUnreadCount(UUID userId) {
        Long count = notificationRepository
                .countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
        return UnreadCountResponse.builder()
                .unreadCount(count)
                .build();
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
        log.info("All notifications marked as read for user: {}", userId);
    }

    @Transactional
    public void deleteNotification(UUID notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setIsDeleted(true);
                    notificationRepository.save(notification);
                });
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}