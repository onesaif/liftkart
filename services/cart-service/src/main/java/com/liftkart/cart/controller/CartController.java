package com.liftkart.cart.controller;

import com.liftkart.cart.dto.request.AddToCartRequest;
import com.liftkart.cart.dto.request.UpdateCartItemRequest;
import com.liftkart.cart.dto.response.ApiResponse;
import com.liftkart.cart.dto.response.CartResponse;
import com.liftkart.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current customer cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("X-User-Id") UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success("Cart fetched",
                cartService.getCart(customerId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader("X-User-Id") UUID customerId,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart",
                cartService.addItem(customerId, request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @RequestHeader("X-User-Id") UUID customerId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated",
                cartService.updateItem(customerId, itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader("X-User-Id") UUID customerId,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed",
                cartService.removeItem(customerId, itemId)));
    }

    @PutMapping("/items/{itemId}/save-for-later")
    @Operation(summary = "Toggle save for later")
    public ResponseEntity<ApiResponse<CartResponse>> saveForLater(
            @RequestHeader("X-User-Id") UUID customerId,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.success("Item updated",
                cartService.saveForLater(customerId, itemId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") UUID customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}