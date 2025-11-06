package com.arka.order_service.infrastructure.persistence.repositories;

import com.arka.order_service.domain.entities.OrderStatus;
import com.arka.order_service.infrastructure.persistence.model.OrderJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJPARepository extends JpaRepository<OrderJPA, Long> {

    Optional<OrderJPA> findByOrderNumber(String orderNumber);

    List<OrderJPA> findByCustomerId(Long customerId);

    List<OrderJPA> findByStatus(OrderStatus status);

    List<OrderJPA> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    List<OrderJPA> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByOrderNumber(String orderNumber);
}
