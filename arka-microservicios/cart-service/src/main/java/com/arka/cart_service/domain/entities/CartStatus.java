package com.arka.cart_service.domain.entities;

public enum CartStatus {
    ACTIVE,      // Carrito activo
    ABANDONED,   // Carrito abandonado (sin actividad por X días)
    CONVERTED,   // Convertido a orden
    EXPIRED      // Expirado (muy antiguo, se puede eliminar)
}
