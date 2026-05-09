package com.liftkart.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDashboardResponse {
    private Integer totalOrdersToday;
    private BigDecimal totalRevenueToday;
    private Integer totalOrdersThisMonth;
    private BigDecimal totalRevenueThisMonth;
    private List<SalesSummaryResponse> last30Days;
}