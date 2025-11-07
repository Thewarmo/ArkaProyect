package com.arka.cart_service.interfaces.controllers;

import com.arka.cart_service.application.dto.AbandonedCartDTO;
import com.arka.cart_service.application.dto.AddItemRequest;
import com.arka.cart_service.application.dto.CartResponse;
import com.arka.cart_service.application.usecases.AddItemToCartUseCase;
import com.arka.cart_service.application.usecases.GetAbandonedCartsUseCase;
import com.arka.cart_service.application.usecases.GetOrCreateCartUseCase;
import com.arka.cart_service.domain.repositories.CartRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final GetOrCreateCartUseCase getOrCreateCartUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final GetAbandonedCartsUseCase getAbandonedCartsUseCase;
    private final CartRepository cartRepository;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CartResponse> getCartByCustomer(@PathVariable Long customerId) {
        log.info("GET /api/v1/carts/customer/{} - Get cart by customer", customerId);
        CartResponse cart = getOrCreateCartUseCase.execute(customerId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/customer/{customerId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable Long customerId,
            @Valid @RequestBody AddItemRequest request) {
        log.info("POST /api/v1/carts/customer/{}/items - Add item to cart", customerId);
        CartResponse cart = addItemToCartUseCase.execute(customerId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable String cartId,
            @PathVariable String itemId) {
        log.info("DELETE /api/v1/carts/{}/items/{} - Remove item from cart", cartId, itemId);

        return cartRepository.findById(cartId)
                .map(cart -> {
                    cart.removeItem(itemId);
                    cartRepository.save(cart);
                    return ResponseEntity.ok(mapToResponse(cart));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        log.info("DELETE /api/v1/carts/customer/{} - Clear cart", customerId);

        return cartRepository.findActiveByCustomerId(customerId)
                .map(cart -> {
                    cart.clear();
                    cartRepository.save(cart);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/abandoned")
    public ResponseEntity<List<AbandonedCartDTO>> getAbandonedCarts() {
        log.info("GET /api/v1/carts/abandoned - Get abandoned carts");
        List<AbandonedCartDTO> abandonedCarts = getAbandonedCartsUseCase.execute();
        return ResponseEntity.ok(abandonedCarts);
    }

    private CartResponse mapToResponse(com.arka.cart_service.domain.entities.Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .status(cart.getStatus())
                .items(cart.getItems().stream()
                        .map(item -> new com.arka.cart_service.application.dto.CartItemDTO(
                                item.getId(),
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .toList())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .createdAt(cart.getCreatedAt())
                .lastUpdatedAt(cart.getLastUpdatedAt())
                .build();
    }
}
