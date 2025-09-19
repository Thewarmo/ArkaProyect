package com.arka.product_service.infrastructure.persistence.repositories;

import com.arka.product_service.domain.entities.Product;
import com.arka.product_service.infrastructure.persistence.model.ProductJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductJPARepository extends JpaRepository<ProductJPA,Long> {

    List<ProductJPA> findByNameContainingIgnoreCase(String name);

    List<ProductJPA> findByCategoryId(Long categoryId);

    List<ProductJPA> findByBrandId(Long brandId);

    List<ProductJPA> findByStockLessThan(Integer threshold);

    @Query("SELECT p FROM ProductJPA p WHERE p.stock > 0")
    List<ProductJPA> findAvailableProducts();

    boolean existsByNameIgnoreCase(String name);

    Page<ProductJPA> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ProductJPA> findByCategoryId(Long categoryId, Pageable pageable);

    Page<ProductJPA> findByBrandId(Long brandId, Pageable pageable);

    @Query("SELECT p FROM ProductJPA p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:brandId IS NULL OR p.brandId = :brandId)")
    Page<ProductJPA> findProductsWithFilters(@Param("name") String name,
                                             @Param("categoryId") Long categoryId,
                                             @Param("brandId") Long brandId,
                                             Pageable pageable);
}
