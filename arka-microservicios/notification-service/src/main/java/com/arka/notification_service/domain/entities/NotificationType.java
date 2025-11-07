package com.arka.notification_service.domain.entities;

public enum NotificationType {
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_IN_TRANSIT,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    LOW_STOCK_ALERT,
    CART_ABANDONED
}
