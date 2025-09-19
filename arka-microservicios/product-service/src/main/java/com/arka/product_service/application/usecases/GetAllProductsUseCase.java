package com.arka.product_service.application.usecases;

import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllProductsUseCase {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> execute() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
