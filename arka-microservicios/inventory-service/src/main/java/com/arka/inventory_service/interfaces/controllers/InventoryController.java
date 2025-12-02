package com.arka.inventory_service.interfaces.controllers;

import com.arka.inventory_service.application.dto.*;
import com.arka.inventory_service.application.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final GetInventoryByProductIdUseCase getInventoryByProductIdUseCase;
    private final CreateInventoryUseCase createInventoryUseCase;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = createInventoryUseCase.execute(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(getInventoryByProductIdUseCase.execute(productId));
    }

    @PostMapping("/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        return ResponseEntity.ok(reserveStockUseCase.execute(request));
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@Valid @RequestBody ReleaseStockRequest request) {
        releaseStockUseCase.execute(request);
        return ResponseEntity.ok().build();
    }
}
