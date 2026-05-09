package com.liftkart.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_price", precision = 15, scale = 2)
    private BigDecimal comparePrice;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Builder.Default
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10;

    private String sku;

    @Builder.Default
    @Column(nullable = false)
    private String status = "DRAFT";   // DRAFT, ACTIVE, INACTIVE

    @Builder.Default
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Review> reviews;
}