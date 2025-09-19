package com.arka.product_service.infrastructure.persistence.repositories;

import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.domain.repositories.ProductRepository;
import com.arka.product_service.infrastructure.persistence.mappers.ProductEntityMapper;
import com.arka.product_service.infrastructure.persistence.model.ProductJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJPARepository productJPARepository;
    private final ProductEntityMapper entityMapper;

    @Override
    public Optional<Product> findById(Long id) {
        return productJPARepository.findById(id)
                .map(entityMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productJPARepository.findAll().stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByName(String name) {
        return productJPARepository.findByNameContainingIgnoreCase(name).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productJPARepository.findByCategoryId(categoryId).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByBrandId(Long brandId) {
        return productJPARepository.findByBrandId(brandId).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByStockLessThan(Integer threshold) {
        return productJPARepository.findByStockLessThan(threshold).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        ProductJPA jpaEntity = entityMapper.toJPA(product);
        ProductJPA savedEntity = productJPARepository.save(jpaEntity);
        return entityMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        productJPARepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return productJPARepository.existsById(id);
    }

}
