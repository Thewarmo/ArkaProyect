package com.arka.notification_service.application.dto;

import com.arka.notification_service.domain.entities.NotificationStatus;
import com.arka.notification_service.domain.entities.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private NotificationStatus status;
    private String errorMessage;
    private Integer retryCount;
    private Long orderId;
    private String orderNumber;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
