package com.liftkart.analytics.controller;

import com.liftkart.analytics.dto.response.ApiResponse;
import com.liftkart.analytics.dto.response.PlatformDashboardResponse;
import com.liftkart.analytics.dto.response.VendorDashboardResponse;
import com.liftkart.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Sales and platform analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/vendor")
    @Operation(summary = "Get vendor dashboard analytics")
    public ResponseEntity<ApiResponse<VendorDashboardResponse>> getVendorDashboard(
            @RequestHeader("X-User-Id") UUID vendorId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Vendor dashboard fetched",
                analyticsService.getVendorDashboard(vendorId)));
    }

    @GetMapping("/platform")
    @Operation(summary = "Get platform-wide analytics (Admin)")
    public ResponseEntity<ApiResponse<PlatformDashboardResponse>> getPlatformDashboard() {

        return ResponseEntity.ok(ApiResponse.success(
                "Platform dashboard fetched",
                analyticsService.getPlatformDashboard()));
    }
}