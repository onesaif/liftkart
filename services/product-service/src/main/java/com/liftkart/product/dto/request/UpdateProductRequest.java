package com.liftkart.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    @Size(min = 2, max = 255)
    private String name;

    private UUID categoryId;

    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private BigDecimal comparePrice;

    @Min(value = 0)
    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private String sku;

    private String status;
}