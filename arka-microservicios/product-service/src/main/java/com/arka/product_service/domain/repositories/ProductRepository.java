package com.arka.product_service.domain.repositories;

import com.arka.product_service.domain.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findByName(String name);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByBrandId(Long brandId);
    List<Product> findByStockLessThan(Integer threshold);
    Product save(Product product);
    void deleteById(Long id);
    boolean existsById(Long id);
}
