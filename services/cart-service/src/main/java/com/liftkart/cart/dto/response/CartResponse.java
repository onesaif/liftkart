package com.liftkart.cart.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID id;
    private UUID customerId;
    private List<CartItemResponse> items;
    private List<CartItemResponse> savedForLater;
    private Integer totalItems;
    private BigDecimal subtotal;
}