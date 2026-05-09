package com.liftkart.product.repository;

import com.liftkart.product.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndIsDeletedFalse(UUID productId, Pageable pageable);

    Optional<Review> findByProductIdAndCustomerIdAndIsDeletedFalse(
            UUID productId, UUID customerId);

    boolean existsByProductIdAndCustomerId(UUID productId, UUID customerId);

    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false")
    Double calculateAverageRating(@Param("productId") UUID productId);

    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isDeleted = false")
    Long countByProductId(@Param("productId") UUID productId);
}