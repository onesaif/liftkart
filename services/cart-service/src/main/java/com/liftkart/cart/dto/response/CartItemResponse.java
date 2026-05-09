package com.liftkart.cart.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal totalPrice;
    private Boolean savedForLater;
}