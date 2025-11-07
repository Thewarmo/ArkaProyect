package com.arka.cart_service.application.usecases;

import com.arka.cart_service.application.dto.AddItemRequest;
import com.arka.cart_service.application.dto.CartResponse;
import com.arka.cart_service.domain.entities.Cart;
import com.arka.cart_service.domain.entities.CartItem;
import com.arka.cart_service.domain.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddItemToCartUseCase {

    private final CartRepository cartRepository;
    private final GetOrCreateCartUseCase getOrCreateCartUseCase;
    private final WebClient productServiceWebClient;

    public CartResponse execute(Long customerId, AddItemRequest request) {
        log.info("Adding item to cart: customerId={}, productId={}, quantity={}",
                customerId, request.getProductId(), request.getQuantity());

        // 1. Obtener información del producto
        ProductInfo productInfo = getProductInfo(request.getProductId());

        // 2. Obtener o crear carrito
        CartResponse cartResponse = getOrCreateCartUseCase.execute(customerId);
        Cart cart = cartRepository.findById(cartResponse.getId())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        // 3. Agregar item al carrito
        CartItem newItem = CartItem.builder()
                .productId(request.getProductId())
                .productName(productInfo.name)
                .quantity(request.getQuantity())
                .unitPrice(productInfo.price)
                .build();

        cart.addItem(newItem);

        // 4. Guardar carrito
        Cart savedCart = cartRepository.save(cart);

        log.info("Item added successfully to cart: {}", savedCart.getId());
        return mapToResponse(savedCart);
    }

    private ProductInfo getProductInfo(Long productId) {
        try {
            ProductResponse product = productServiceWebClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();

            if (product == null) {
                throw new RuntimeException("Producto no encontrado: " + productId);
            }

            return new ProductInfo(product.getName(), product.getPrice());
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Error al obtener información del producto: " + e.getMessage());
        }
    }

    private CartResponse mapToResponse(Cart cart) {
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

    // Inner classes
    private record ProductInfo(String name, BigDecimal price) {}

    private static class ProductResponse {
        public String name;
        public BigDecimal price;

        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
    }
}
