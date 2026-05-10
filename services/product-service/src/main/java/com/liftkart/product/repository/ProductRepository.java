package com.liftkart.product.repository;

import com.liftkart.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    Optional<Product> findBySlugAndIsDeletedFalse(String slug);

    Page<Product> findByStatusAndIsDeletedFalse(String status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatusAndIsDeletedFalse(
            UUID categoryId, String status, Pageable pageable);

    List<Product> findByVendorIdAndIsDeletedFalse(UUID vendorId);

    boolean existsBySlug(String slug);

    @Query("""
        SELECT p FROM Product p
        WHERE p.isDeleted = false
        AND p.status = 'ACTIVE'
        AND (COALESCE(:keyword, '') = ''
             OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (COALESCE(CAST(:categoryId AS string), '') = ''
             OR p.category.id = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        ORDER BY p.createdAt DESC
        """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}