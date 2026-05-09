package com.liftkart.analytics.repository;

import com.liftkart.analytics.entity.ProductViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductViewEventRepository
        extends JpaRepository<ProductViewEvent, UUID> {

    Long countByProductId(UUID productId);

    @Query("SELECT p.productId, COUNT(p) as views " +
            "FROM ProductViewEvent p " +
            "GROUP BY p.productId " +
            "ORDER BY views DESC")
    List<Object[]> findTopViewedProducts();
}