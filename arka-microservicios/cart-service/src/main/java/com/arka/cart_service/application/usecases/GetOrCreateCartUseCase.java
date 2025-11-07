package com.arka.cart_service.application.usecases;

import com.arka.cart_service.application.dto.CartItemDTO;
import com.arka.cart_service.application.dto.CartResponse;
import com.arka.cart_service.domain.entities.Cart;
import com.arka.cart_service.domain.entities.CartStatus;
import com.arka.cart_service.domain.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetOrCreateCartUseCase {

    private final CartRepository cartRepository;

    public CartResponse execute(Long customerId) {
        log.info("Getting or creating cart for customer: {}", customerId);

        // Buscar carrito activo existente
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseGet(() -> {
                    // Crear nuevo carrito
                    log.info("Creating new cart for customer: {}", customerId);
                    Cart newCart = Cart.builder()
                            .id(UUID.randomUUID().toString())
                            .customerId(customerId)
                            .status(CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .totalAmount(BigDecimal.ZERO)
                            .totalItems(0)
                            .createdAt(LocalDateTime.now())
                            .lastUpdatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });

        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems() != null ?
                cart.getItems().stream()
                        .map(item -> CartItemDTO.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .status(cart.getStatus())
                .items(itemDTOs)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .createdAt(cart.getCreatedAt())
                .lastUpdatedAt(cart.getLastUpdatedAt())
                .build();
    }
}
