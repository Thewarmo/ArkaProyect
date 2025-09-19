package com.arka.product_service.interfaces.controllers;


import com.arka.product_service.application.dto.*;
import com.arka.product_service.application.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetAllProductsUseCase getAllProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final GetLowStockProductsUseCase getLowStockProductsUseCase;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = createProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = getAllProductsUseCase.execute();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Optional<ProductResponse> product = getProductByIdUseCase.execute(id);
        return product
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {

        Optional<ProductResponse> updatedProduct = updateProductStockUseCase.execute(id, request);
        return updatedProduct
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<LowStockReportResponse> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId) {

        LowStockReportRequest request = new LowStockReportRequest();
        request.setThreshold(threshold);
        request.setCategoryId(categoryId);
        request.setBrandId(brandId);

        LowStockReportResponse report = getLowStockProductsUseCase.execute(request);
        return ResponseEntity.ok(report);
    }


}
