package com.arka.product_service.application.usecases;

import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Optional<ProductResponse> execute(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(productMapper::toResponse);
    }
}
