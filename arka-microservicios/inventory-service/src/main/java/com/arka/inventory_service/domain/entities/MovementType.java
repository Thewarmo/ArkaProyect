package com.arka.inventory_service.domain.entities;

/**
 * Enum que representa los tipos de movimiento de inventario
 */
public enum MovementType {
    /**
     * Entrada de mercancía (compra a proveedor, devolución de cliente)
     */
    IN,

    /**
     * Salida de mercancía (venta, devolución a proveedor)
     */
    OUT,

    /**
     * Reserva temporal (orden pendiente de confirmación)
     */
    RESERVED,

    /**
     * Liberación de reserva (orden cancelada)
     */
    RELEASED,

    /**
     * Ajuste manual de inventario
     */
    ADJUSTMENT
}
