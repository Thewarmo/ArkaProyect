package com.arka.product_service.domain.services;

import com.arka.product_service.domain.entities.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductDomainServices {

    public void validateProductForCreation(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (product.getName().length() < 3 || product.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }

    public void validateStockOperation(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }

        if (!product.isAvailable()) {
            throw new IllegalStateException("El producto no está disponible");
        }

        if (product.getStock() < quantity) {
            throw new IllegalStateException("Stock insuficiente");
        }
    }

    public void validateStockUpdate(Product product, Integer newStock) {
        if (newStock == null) {
            throw new IllegalArgumentException("El nuevo stock no puede ser nulo");
        }

        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        // Validación adicional: no permitir cambios drásticos sin motivo
        int currentStock = product.getStock();
        int difference = Math.abs(newStock - currentStock);

        if (difference > currentStock * 2) {
            throw new IllegalArgumentException("Cambio de stock muy drástico, requiere aprobación adicional");
        }
    }
}
