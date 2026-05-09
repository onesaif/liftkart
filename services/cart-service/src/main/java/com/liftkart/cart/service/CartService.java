package com.liftkart.cart.service;

import com.liftkart.cart.dto.request.AddToCartRequest;
import com.liftkart.cart.dto.request.UpdateCartItemRequest;
import com.liftkart.cart.dto.response.CartItemResponse;
import com.liftkart.cart.dto.response.CartResponse;
import com.liftkart.cart.entity.Cart;
import com.liftkart.cart.entity.CartItem;
import com.liftkart.cart.exception.BadRequestException;
import com.liftkart.cart.exception.ResourceNotFoundException;
import com.liftkart.cart.repository.CartItemRepository;
import com.liftkart.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // ── Get or Create Cart ─────────────────────────────────────────
    public CartResponse getCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        return mapToResponse(cart);
    }

    // ── Add Item to Cart ───────────────────────────────────────────
    @Transactional
    public CartResponse addItem(UUID customerId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(customerId);

        // Check if item already exists in cart
        cartItemRepository.findByCartIdAndProductIdAndIsDeletedFalse(
                        cart.getId(), request.getProductId())
                .ifPresentOrElse(
                        existingItem -> {
                            // Update quantity if already in cart
                            existingItem.setQuantity(
                                    existingItem.getQuantity() + request.getQuantity());
                            cartItemRepository.save(existingItem);
                        },
                        () -> {
                            // Add new item with price snapshot
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .productId(request.getProductId())
                                    .productName(request.getProductName())
                                    .imageUrl(request.getImageUrl())
                                    .quantity(request.getQuantity())
                                    .priceSnapshot(request.getPrice())
                                    .build();
                            item.setCreatedBy(customerId);
                            cart.getItems().add(item);
                        }
                );

        cart.setLastActivityAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Item added to cart for customer: {}", customerId);
        return mapToResponse(cart);
    }

    // ── Update Item Quantity ───────────────────────────────────────
    @Transactional
    public CartResponse updateItem(UUID customerId, UUID itemId,
                                   UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        cart.setLastActivityAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    // ── Remove Item ────────────────────────────────────────────────
    @Transactional
    public CartResponse removeItem(UUID customerId, UUID itemId) {
        Cart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        item.setIsDeleted(true);
        cartItemRepository.save(item);

        return mapToResponse(cart);
    }

    // ── Save for Later ─────────────────────────────────────────────
    @Transactional
    public CartResponse saveForLater(UUID customerId, UUID itemId) {
        Cart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found"));

        item.setSavedForLater(!item.getSavedForLater());
        cartItemRepository.save(item);

        return mapToResponse(cart);
    }

    // ── Clear Cart ─────────────────────────────────────────────────
    @Transactional
    public void clearCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().forEach(item -> item.setIsDeleted(true));
        cartRepository.save(cart);
        log.info("Cart cleared for customer: {}", customerId);
    }

    // ── Get Cart for Checkout ──────────────────────────────────────
    public Cart getCartEntity(UUID customerId) {
        return cartRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found or empty"));
    }

    // ── Private Helpers ────────────────────────────────────────────
    private Cart getOrCreateCart(UUID customerId) {
        return cartRepository
                .findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .customerId(customerId)
                            .lastActivityAt(LocalDateTime.now())
                            .build();
                    newCart.setCreatedBy(customerId);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> activeItems = cart.getItems().stream()
                .filter(item -> !item.getIsDeleted() && !item.getSavedForLater())
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        List<CartItemResponse> savedItems = cart.getItems().stream()
                .filter(item -> !item.getIsDeleted() && item.getSavedForLater())
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = activeItems.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .items(activeItems)
                .savedForLater(savedItems)
                .totalItems(activeItems.stream()
                        .mapToInt(CartItemResponse::getQuantity).sum())
                .subtotal(subtotal)
                .build();
    }

    private CartItemResponse mapItemToResponse(CartItem item) {
        BigDecimal totalPrice = item.getPriceSnapshot()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .imageUrl(item.getImageUrl())
                .quantity(item.getQuantity())
                .priceSnapshot(item.getPriceSnapshot())
                .totalPrice(totalPrice)
                .savedForLater(item.getSavedForLater())
                .build();
    }
}