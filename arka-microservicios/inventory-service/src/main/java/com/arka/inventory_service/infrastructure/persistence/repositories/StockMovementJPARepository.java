package com.arka.inventory_service.infrastructure.persistence.repositories;

import com.arka.inventory_service.domain.entities.MovementType;
import com.arka.inventory_service.infrastructure.persistence.model.StockMovementJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementJPARepository extends JpaRepository<StockMovementJPA, Long> {

    List<StockMovementJPA> findByProductId(Long productId);

    List<StockMovementJPA> findByMovementType(MovementType movementType);

    List<StockMovementJPA> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<StockMovementJPA> findByProductIdAndCreatedAtBetween(Long productId, LocalDateTime startDate, LocalDateTime endDate);
}
