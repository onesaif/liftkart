package com.liftkart.analytics.service;

import com.liftkart.analytics.dto.response.PlatformDashboardResponse;
import com.liftkart.analytics.dto.response.SalesSummaryResponse;
import com.liftkart.analytics.dto.response.VendorDashboardResponse;
import com.liftkart.analytics.entity.DailySalesSummary;
import com.liftkart.analytics.repository.DailySalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DailySalesSummaryRepository dailySalesSummaryRepository;

    public VendorDashboardResponse getVendorDashboard(UUID vendorId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate monthStart = today.withDayOfMonth(1);

        // Today's stats
        DailySalesSummary todaySummary = dailySalesSummaryRepository
                .findByVendorIdAndSummaryDate(vendorId, today)
                .orElse(emptyDailySummary(vendorId, today));

        // This month's stats
        List<DailySalesSummary> monthSummaries = dailySalesSummaryRepository
                .findByVendorIdAndSummaryDateBetweenOrderBySummaryDateAsc(
                        vendorId, monthStart, today);

        Integer totalOrdersMonth = monthSummaries.stream()
                .mapToInt(DailySalesSummary::getTotalOrders).sum();
        BigDecimal totalRevenueMonth = monthSummaries.stream()
                .map(DailySalesSummary::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Last 7 days
        List<DailySalesSummary> last7Days = dailySalesSummaryRepository
                .findByVendorIdAndSummaryDateBetweenOrderBySummaryDateAsc(
                        vendorId, sevenDaysAgo, today);

        return VendorDashboardResponse.builder()
                .totalOrdersToday(todaySummary.getTotalOrders())
                .totalRevenueToday(todaySummary.getTotalRevenue())
                .totalOrdersThisMonth(totalOrdersMonth)
                .totalRevenueThisMonth(totalRevenueMonth)
                .last7Days(last7Days.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public PlatformDashboardResponse getPlatformDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate monthStart = today.withDayOfMonth(1);

        DailySalesSummary todaySummary = dailySalesSummaryRepository
                .findByVendorIdIsNullAndSummaryDate(today)
                .orElse(emptyDailySummary(null, today));

        List<DailySalesSummary> monthSummaries = dailySalesSummaryRepository
                .findPlatformSummary(monthStart, today);

        Integer totalOrdersMonth = monthSummaries.stream()
                .mapToInt(DailySalesSummary::getTotalOrders).sum();
        BigDecimal totalRevenueMonth = monthSummaries.stream()
                .map(DailySalesSummary::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DailySalesSummary> last30Days = dailySalesSummaryRepository
                .findPlatformSummary(thirtyDaysAgo, today);

        return PlatformDashboardResponse.builder()
                .totalOrdersToday(todaySummary.getTotalOrders())
                .totalRevenueToday(todaySummary.getTotalRevenue())
                .totalOrdersThisMonth(totalOrdersMonth)
                .totalRevenueThisMonth(totalRevenueMonth)
                .last30Days(last30Days.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private DailySalesSummary emptyDailySummary(UUID vendorId,
                                                LocalDate date) {
        return DailySalesSummary.builder()
                .vendorId(vendorId)
                .summaryDate(date)
                .totalOrders(0)
                .totalRevenue(BigDecimal.ZERO)
                .totalItemsSold(0)
                .build();
    }

    private SalesSummaryResponse mapToResponse(DailySalesSummary summary) {
        return SalesSummaryResponse.builder()
                .vendorId(summary.getVendorId())
                .summaryDate(summary.getSummaryDate())
                .totalOrders(summary.getTotalOrders())
                .totalRevenue(summary.getTotalRevenue())
                .totalItemsSold(summary.getTotalItemsSold())
                .build();
    }
}