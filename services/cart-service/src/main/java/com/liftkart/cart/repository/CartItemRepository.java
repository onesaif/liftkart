package com.liftkart.cart.repository;

import com.liftkart.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndProductIdAndSavedForLaterFalseAndIsDeletedFalse(
            UUID cartId, UUID productId);

    void deleteByCartId(UUID cartId);
}