package com.liftkart.notification.repository;

import com.liftkart.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID userId, Pageable pageable);

    Long countByUserIdAndIsReadFalseAndIsDeletedFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") UUID userId);
}