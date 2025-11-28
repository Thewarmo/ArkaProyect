package com.arka.inventory_service.infrastructure.persistence.repositories;

import com.arka.inventory_service.domain.entities.MovementType;
import com.arka.inventory_service.domain.entities.StockMovement;
import com.arka.inventory_service.domain.repositories.StockMovementRepository;
import com.arka.inventory_service.infrastructure.persistence.mappers.StockMovementEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final StockMovementJPARepository jpaRepository;
    private final StockMovementEntityMapper mapper;

    @Override
    public Optional<StockMovement> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<StockMovement> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovement> findByMovementType(MovementType movementType) {
        return jpaRepository.findByMovementType(movementType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovement> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByCreatedAtBetween(startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovement> findByProductIdAndDateRange(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByProductIdAndCreatedAtBetween(productId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public StockMovement save(StockMovement movement) {
        return mapper.toDomain(jpaRepository.save(mapper.toJPA(movement)));
    }

    @Override
    public List<StockMovement> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
