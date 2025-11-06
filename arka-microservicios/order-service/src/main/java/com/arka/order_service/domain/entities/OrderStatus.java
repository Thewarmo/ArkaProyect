package com.arka.order_service.domain.entities;

/**
 * Enum que representa los estados de una orden
 */
public enum OrderStatus {
    /**
     * Orden creada pero no confirmada (modificable)
     */
    PENDING,

    /**
     * Orden confirmada (no modificable, stock reservado)
     */
    CONFIRMED,

    /**
     * Orden en tránsito/despacho
     */
    IN_TRANSIT,

    /**
     * Orden entregada
     */
    DELIVERED,

    /**
     * Orden cancelada (stock liberado)
     */
    CANCELLED
}
