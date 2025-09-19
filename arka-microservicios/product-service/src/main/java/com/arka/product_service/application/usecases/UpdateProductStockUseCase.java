package com.arka.product_service.application.usecases;

import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.dto.UpdateStockRequest;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.entities.StockHistoryEntry;
import com.arka.product_service.domain.repositories.ProductRepository;
import com.arka.product_service.domain.services.ProductDomainServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateProductStockUseCase {

    private final ProductRepository productRepository;
    private final ProductDomainServices productDomainService;
    private final ProductMapper productMapper;

    public Optional<ProductResponse> execute(Long productId, UpdateStockRequest request) {
        // 1. Buscar el producto
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return Optional.empty();
        }

        Product product = productOpt.get();
        Integer previousStock = product.getStock();

        // 2. Validar el cambio de stock
        productDomainService.validateStockUpdate(product, request.getNewStock());

        // 3. Actualizar stock
        product.setStock(request.getNewStock());
        product.setUpdatedAt(LocalDateTime.now());

        // 4. Crear registro del historial (por simplicidad, lo guardamos como log)
        StockHistoryEntry historyEntry = new StockHistoryEntry(
                productId,
                previousStock,
                request.getNewStock(),
                request.getReason()
        );

        // 5. Persistir cambios
        Product updatedProduct = productRepository.save(product);

        // Log del cambio (en un sistema real, esto iría a una tabla separada)
        logStockChange(historyEntry);

        return Optional.of(productMapper.toResponse(updatedProduct));
    }

    private void logStockChange(StockHistoryEntry entry) {
        System.out.println(String.format(
                "Stock actualizado - Producto: %d, Stock anterior: %d, Nuevo stock: %d, Diferencia: %+d, Motivo: %s",
                entry.getProductId(),
                entry.getPreviousStock(),
                entry.getNewStock(),
                entry.getStockDifference(),
                entry.getReason() != null ? entry.getReason() : "Sin motivo especificado"
        ));
    }
}
