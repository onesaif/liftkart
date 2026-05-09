package com.liftkart.cart.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AddToCartRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String imageUrl;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
}