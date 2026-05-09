package com.liftkart.product.repository;

import com.liftkart.product.entity.InventoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, UUID> {

    Page<InventoryLog> findByProductIdOrderByCreatedAtDesc(
            UUID productId, Pageable pageable);
}