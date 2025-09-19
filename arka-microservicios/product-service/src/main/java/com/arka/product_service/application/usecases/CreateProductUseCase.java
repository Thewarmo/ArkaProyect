package com.arka.product_service.application.usecases;


import com.arka.product_service.application.dto.CreateProductRequest;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import com.arka.product_service.domain.services.ProductDomainServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductDomainServices productDomainService;
    private final ProductMapper productMapper;

    public ProductResponse execute(CreateProductRequest request) {
        // 1. Mapear del DTO a entidad de dominio
        Product product = productMapper.toEntity(request);

        // 2. Validar reglas de negocio
        productDomainService.validateProductForCreation(product);

        // 3. Persistir
        Product savedProduct = productRepository.save(product);

        // 4. Retornar respuesta
        return productMapper.toResponse(savedProduct);
    }

}
