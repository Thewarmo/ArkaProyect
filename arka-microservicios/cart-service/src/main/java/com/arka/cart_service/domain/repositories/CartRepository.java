package com.arka.cart_service.domain.repositories;

import com.arka.cart_service.domain.entities.Cart;
import com.arka.cart_service.domain.entities.CartStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findById(String id);
    Optional<Cart> findActiveByCustomerId(Long customerId);
    List<Cart> findByStatus(CartStatus status);
    List<Cart> findAbandonedCarts(LocalDateTime thresholdDate);
    void deleteById(String id);
}
