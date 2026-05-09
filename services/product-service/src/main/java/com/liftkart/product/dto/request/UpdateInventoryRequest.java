package com.liftkart.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateInventoryRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotBlank(message = "Reason is required")
    private String reason;     // RESTOCK, ADJUSTMENT
}