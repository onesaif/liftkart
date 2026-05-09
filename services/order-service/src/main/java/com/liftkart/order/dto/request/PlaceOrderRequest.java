package com.liftkart.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Address is required")
    private UUID addressId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String notes;

    @NotNull(message = "Items are required")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private UUID productId;
        private String productName;
        private String imageUrl;
        private BigDecimal price;
        private Integer quantity;
        private UUID vendorId;
    }
}