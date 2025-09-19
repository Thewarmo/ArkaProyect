package com.arka.product_service.application.usecases;

import com.arka.product_service.application.dto.PagedResponse;
import com.arka.product_service.application.dto.ProductResponse;
import com.arka.product_service.application.ports.ProductMapper;
import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.infrastructure.persistence.mappers.ProductEntityMapper;
import com.arka.product_service.infrastructure.persistence.model.ProductJPA;
import com.arka.product_service.infrastructure.persistence.repositories.ProductJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllProductsPaginatedUseCase {

    private final ProductJPARepository productJPARepository;
    private final ProductEntityMapper productEntityMapper; // Mapper JPA -> Domain
    private final ProductMapper productMapper; // Mapper Domain -> DTO

    public PagedResponse<ProductResponse> execute(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String name,
            Long categoryId,
            Long brandId) {

        // Validar parámetros
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "name";

        // Crear Sort
        Sort.Direction direction = sortDir != null && sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        // Crear Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Buscar con filtros
        Page<ProductJPA> productPage = productJPARepository.findProductsWithFilters(
                name, categoryId, brandId, pageable
        );

        // Convertir usando los mappers existentes
        List<ProductResponse> productResponses = productPage.getContent()
                .stream()
                .map(productEntityMapper::toDomain)  // JPA -> Domain
                .map(productMapper::toResponse)      // Domain -> DTO
                .collect(Collectors.toList());

        // Crear respuesta paginada
        return new PagedResponse<>(
                productResponses,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }
}