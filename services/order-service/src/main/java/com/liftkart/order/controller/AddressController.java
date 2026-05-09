package com.liftkart.order.controller;

import com.liftkart.order.dto.request.CreateAddressRequest;
import com.liftkart.order.dto.response.AddressResponse;
import com.liftkart.order.dto.response.ApiResponse;
import com.liftkart.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Delivery address management")
public class AddressController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Add a new delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @RequestHeader("X-User-Id") UUID customerId,
            @Valid @RequestBody CreateAddressRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added",
                        orderService.createAddress(customerId, request)));
    }

    @GetMapping
    @Operation(summary = "Get all addresses for current customer")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @RequestHeader("X-User-Id") UUID customerId) {

        return ResponseEntity.ok(ApiResponse.success("Addresses fetched",
                orderService.getAddresses(customerId)));
    }
}