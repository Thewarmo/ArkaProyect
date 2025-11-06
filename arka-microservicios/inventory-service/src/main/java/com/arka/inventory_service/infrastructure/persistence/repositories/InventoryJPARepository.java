package com.arka.inventory_service.infrastructure.persistence.repositories;

import com.arka.inventory_service.infrastructure.persistence.model.InventoryJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryJPARepository extends JpaRepository<InventoryJPA, Long> {

    Optional<InventoryJPA> findByProductId(Long productId);

    @Query("SELECT i FROM InventoryJPA i WHERE (i.availableStock + i.reservedStock) <= i.minimumStock")
    List<InventoryJPA> findLowStockProducts();

    @Query("SELECT i FROM InventoryJPA i WHERE i.availableStock = 0")
    List<InventoryJPA> findOutOfStockProducts();

    boolean existsByProductId(Long productId);
}
