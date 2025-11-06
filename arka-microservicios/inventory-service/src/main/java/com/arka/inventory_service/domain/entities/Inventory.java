package com.arka.inventory_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa el inventario de un producto.
 * Esta clase es un POJO puro sin anotaciones de JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Long id;
    private Long productId;
    private Integer availableStock; // Stock disponible para venta
    private Integer reservedStock; // Stock reservado temporalmente
    private Integer minimumStock; // Stock mínimo para alerta
    private Integer version; // Para control de concurrencia optimista
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Obtiene el stock total (disponible + reservado)
     */
    public Integer getTotalStock() {
        return availableStock + reservedStock;
    }

    /**
     * Verifica si hay stock disponible suficiente
     */
    public boolean hasAvailableStock(Integer quantity) {
        return availableStock >= quantity;
    }

    /**
     * Verifica si el stock está por debajo del mínimo
     */
    public boolean isLowStock() {
        return getTotalStock() <= minimumStock;
    }

    /**
     * Reduce el stock disponible (para reserva o venta)
     */
    public void reduceAvailableStock(Integer quantity) {
        if (!hasAvailableStock(quantity)) {
            throw new IllegalStateException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                    availableStock, quantity));
        }
        this.availableStock -= quantity;
    }

    /**
     * Aumenta el stock disponible (para entrada de mercancía)
     */
    public void increaseAvailableStock(Integer quantity) {
        this.availableStock += quantity;
    }

    /**
     * Reserva stock temporalmente
     */
    public void reserveStock(Integer quantity) {
        if (!hasAvailableStock(quantity)) {
            throw new IllegalStateException(
                String.format("Stock insuficiente para reservar. Disponible: %d, Solicitado: %d",
                    availableStock, quantity));
        }
        this.availableStock -= quantity;
        this.reservedStock += quantity;
    }

    /**
     * Libera stock reservado (devuelve a disponible)
     */
    public void releaseReservedStock(Integer quantity) {
        if (reservedStock < quantity) {
            throw new IllegalStateException(
                String.format("No hay suficiente stock reservado. Reservado: %d, A liberar: %d",
                    reservedStock, quantity));
        }
        this.reservedStock -= quantity;
        this.availableStock += quantity;
    }

    /**
     * Confirma una reserva (mueve de reservado a vendido/eliminado)
     */
    public void confirmReservation(Integer quantity) {
        if (reservedStock < quantity) {
            throw new IllegalStateException(
                String.format("No hay suficiente stock reservado. Reservado: %d, A confirmar: %d",
                    reservedStock, quantity));
        }
        this.reservedStock -= quantity;
    }
}
