package com.liftkart.product.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private UUID vendorId;
    private String categoryName;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private Integer stockQuantity;
    private String sku;
    private String status;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}