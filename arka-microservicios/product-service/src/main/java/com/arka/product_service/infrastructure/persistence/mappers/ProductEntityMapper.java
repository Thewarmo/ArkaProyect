package com.arka.product_service.infrastructure.persistence.mappers;

import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.infrastructure.persistence.model.ProductJPA;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

    public Product toDomain(ProductJPA jpaEntity) {
        Product product = new Product();
        product.setId(jpaEntity.getId());
        product.setName(jpaEntity.getName());
        product.setDescription(jpaEntity.getDescription());
        product.setPrice(jpaEntity.getPrice());
        product.setStock(jpaEntity.getStock());
        product.setCategoryId(jpaEntity.getCategoryId());
        product.setBrandId(jpaEntity.getBrandId());
        product.setCreatedAt(jpaEntity.getCreatedAt());
        product.setUpdatedAt(jpaEntity.getUpdatedAt());
        return product;
    }

    public ProductJPA toJPA(Product domainEntity) {
        ProductJPA jpa = new ProductJPA();
        jpa.setId(domainEntity.getId());
        jpa.setName(domainEntity.getName());
        jpa.setDescription(domainEntity.getDescription());
        jpa.setPrice(domainEntity.getPrice());
        jpa.setStock(domainEntity.getStock());
        jpa.setCategoryId(domainEntity.getCategoryId());
        jpa.setBrandId(domainEntity.getBrandId());
        jpa.setCreatedAt(domainEntity.getCreatedAt());
        jpa.setUpdatedAt(domainEntity.getUpdatedAt());
        return jpa;
    }
}
