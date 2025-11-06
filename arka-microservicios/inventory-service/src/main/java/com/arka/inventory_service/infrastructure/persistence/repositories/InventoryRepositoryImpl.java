package com.arka.inventory_service.infrastructure.persistence.repositories;

import com.arka.inventory_service.domain.entities.Inventory;
import com.arka.inventory_service.domain.repositories.InventoryRepository;
import com.arka.inventory_service.infrastructure.persistence.mappers.InventoryEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryJPARepository jpaRepository;
    private final InventoryEntityMapper mapper;

    @Override
    public Optional<Inventory> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findLowStockProducts() {
        return jpaRepository.findLowStockProducts().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findOutOfStockProducts() {
        return jpaRepository.findOutOfStockProducts().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Inventory save(Inventory inventory) {
        return mapper.toDomain(jpaRepository.save(mapper.toJPA(inventory)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jpaRepository.existsByProductId(productId);
    }
}
