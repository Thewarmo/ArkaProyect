package com.arka.notification_service.infrastructure.persistence.mappers;

import com.arka.notification_service.domain.entities.Notification;
import com.arka.notification_service.infrastructure.persistence.model.NotificationJPA;
import org.springframework.stereotype.Component;

@Component
public class NotificationEntityMapper {

    public Notification toDomain(NotificationJPA jpa) {
        if (jpa == null) {
            return null;
        }

        return Notification.builder()
                .id(jpa.getId())
                .type(jpa.getType())
                .recipientEmail(jpa.getRecipientEmail())
                .recipientName(jpa.getRecipientName())
                .subject(jpa.getSubject())
                .content(jpa.getContent())
                .status(jpa.getStatus())
                .errorMessage(jpa.getErrorMessage())
                .retryCount(jpa.getRetryCount())
                .orderId(jpa.getOrderId())
                .orderNumber(jpa.getOrderNumber())
                .createdAt(jpa.getCreatedAt())
                .sentAt(jpa.getSentAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    public NotificationJPA toJPA(Notification domain) {
        if (domain == null) {
            return null;
        }

        return NotificationJPA.builder()
                .id(domain.getId())
                .type(domain.getType())
                .recipientEmail(domain.getRecipientEmail())
                .recipientName(domain.getRecipientName())
                .subject(domain.getSubject())
                .content(domain.getContent())
                .status(domain.getStatus())
                .errorMessage(domain.getErrorMessage())
                .retryCount(domain.getRetryCount())
                .orderId(domain.getOrderId())
                .orderNumber(domain.getOrderNumber())
                .createdAt(domain.getCreatedAt())
                .sentAt(domain.getSentAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
