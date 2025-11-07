package com.arka.notification_service.domain.exceptions;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(Long id) {
        super("Notification not found with ID: " + id);
    }
}
