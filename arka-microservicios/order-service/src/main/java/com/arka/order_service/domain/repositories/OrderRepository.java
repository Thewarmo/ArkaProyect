package com.arka.order_service.domain.repositories;

import com.arka.order_service.domain.entities.Order;
import com.arka.order_service.domain.entities.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de órdenes
 */
public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findAll();

    Order save(Order order);

    void deleteById(Long id);

    boolean existsByOrderNumber(String orderNumber);
}
