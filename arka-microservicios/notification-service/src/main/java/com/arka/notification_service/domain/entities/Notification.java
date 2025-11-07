package com.arka.notification_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;
    private NotificationType type;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String content;
    private NotificationStatus status;
    private String errorMessage;
    private Integer retryCount;

    // Reference to related entity
    private Long orderId;
    private String orderNumber;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        this.status = NotificationStatus.RETRY;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return this.retryCount == null || this.retryCount < 3;
    }
}
