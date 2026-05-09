package com.liftkart.order.repository;

import com.liftkart.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID customerId, Pageable pageable);

    Optional<Order> findByIdAndCustomerIdAndIsDeletedFalse(
            UUID id, UUID customerId);

    Page<Order> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}