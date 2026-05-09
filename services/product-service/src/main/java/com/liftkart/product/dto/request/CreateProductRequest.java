package com.liftkart.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal comparePrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    private Integer lowStockThreshold = 10;

    private String sku;

    private List<String> imageUrls;
}