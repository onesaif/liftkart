package com.liftkart.order.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {
    private String oldStatus;
    private String newStatus;
    private String changedByRole;
    private String comment;
    private LocalDateTime changedAt;
}