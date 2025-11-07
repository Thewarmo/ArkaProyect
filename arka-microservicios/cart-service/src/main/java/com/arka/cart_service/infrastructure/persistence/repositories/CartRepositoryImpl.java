package com.arka.cart_service.infrastructure.persistence.repositories;

import com.arka.cart_service.domain.entities.Cart;
import com.arka.cart_service.domain.entities.CartItem;
import com.arka.cart_service.domain.entities.CartStatus;
import com.arka.cart_service.domain.repositories.CartRepository;
import com.arka.cart_service.infrastructure.persistence.model.CartDocument;
import com.arka.cart_service.infrastructure.persistence.model.CartItemDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final CartMongoRepository mongoRepository;

    @Override
    public Cart save(Cart cart) {
        CartDocument document = toDocument(cart);
        CartDocument saved = mongoRepository.save(document);
        return toEntity(saved);
    }

    @Override
    public Optional<Cart> findById(String id) {
        return mongoRepository.findById(id).map(this::toEntity);
    }

    @Override
    public Optional<Cart> findActiveByCustomerId(Long customerId) {
        return mongoRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .map(this::toEntity);
    }

    @Override
    public List<Cart> findByStatus(CartStatus status) {
        return mongoRepository.findByStatus(status).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cart> findAbandonedCarts(LocalDateTime thresholdDate) {
        return mongoRepository.findAbandonedCarts(thresholdDate).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    // Mappers
    private CartDocument toDocument(Cart cart) {
        List<CartItemDocument> itemDocuments = null;
        if (cart.getItems() != null) {
            itemDocuments = cart.getItems().stream()
                    .map(this::toItemDocument)
                    .collect(Collectors.toList());
        }

        return CartDocument.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .status(cart.getStatus())
                .items(itemDocuments)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .createdAt(cart.getCreatedAt())
                .lastUpdatedAt(cart.getLastUpdatedAt())
                .build();
    }

    private Cart toEntity(CartDocument document) {
        List<CartItem> items = null;
        if (document.getItems() != null) {
            items = document.getItems().stream()
                    .map(this::toItemEntity)
                    .collect(Collectors.toList());
        }

        return Cart.builder()
                .id(document.getId())
                .customerId(document.getCustomerId())
                .status(document.getStatus())
                .items(items)
                .totalAmount(document.getTotalAmount())
                .totalItems(document.getTotalItems())
                .createdAt(document.getCreatedAt())
                .lastUpdatedAt(document.getLastUpdatedAt())
                .build();
    }

    private CartItemDocument toItemDocument(CartItem item) {
        return CartItemDocument.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private CartItem toItemEntity(CartItemDocument document) {
        return CartItem.builder()
                .id(document.getId())
                .productId(document.getProductId())
                .productName(document.getProductName())
                .quantity(document.getQuantity())
                .unitPrice(document.getUnitPrice())
                .subtotal(document.getSubtotal())
                .build();
    }
}
