package com.liftkart.product.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID customerId;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;
    private LocalDateTime createdAt;
}