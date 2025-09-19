package com.arka.product_service.application.usecases;

import com.arka.product_service.application.dto.LowStockReportRequest;
import com.arka.product_service.application.dto.LowStockReportResponse;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetLowStockProductsUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public LowStockReportResponse execute(LowStockReportRequest request) {
        // 1. Obtener productos con stock bajo
        List<Product> lowStockProducts = productRepository.findByStockLessThan(request.getThreshold());

        // 2. Filtrar por categoría si se especifica
        if (request.getCategoryId() != null) {
            lowStockProducts = lowStockProducts.stream()
                    .filter(p -> request.getCategoryId().equals(p.getCategoryId()))
                    .collect(Collectors.toList());
        }

        // 3. Filtrar por marca si se especifica
        if (request.getBrandId() != null) {
            lowStockProducts = lowStockProducts.stream()
                    .filter(p -> request.getBrandId().equals(p.getBrandId()))
                    .collect(Collectors.toList());
        }

        // 4. Convertir a DTOs
        List<ProductResponse> productResponses = lowStockProducts.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        // 5. Calcular estadísticas
        int totalStockValue = lowStockProducts.stream()
                .mapToInt(p -> p.getPrice().intValue() * p.getStock())
                .sum();

        String criticalityLevel = determineCriticalityLevel(lowStockProducts.size(), request.getThreshold());

        // 6. Crear respuesta
        LowStockReportResponse response = new LowStockReportResponse();
        response.setReportDate(LocalDateTime.now());
        response.setThreshold(request.getThreshold());
        response.setTotalProductsFound(productResponses.size());
        response.setLowStockProducts(productResponses);
        response.setTotalStockValue(totalStockValue);
        response.setCriticalityLevel(criticalityLevel);

        return response;
    }

    private String determineCriticalityLevel(int productsCount, int threshold) {
        if (productsCount == 0) {
            return "NONE";
        } else if (productsCount <= 5) {
            return "LOW";
        } else if (productsCount <= 15) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
}
