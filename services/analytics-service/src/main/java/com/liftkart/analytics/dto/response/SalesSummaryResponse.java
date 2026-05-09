package com.liftkart.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryResponse {
    private UUID vendorId;
    private LocalDate summaryDate;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private Integer totalItemsSold;
}