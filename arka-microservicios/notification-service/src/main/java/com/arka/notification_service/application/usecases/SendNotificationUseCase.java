package com.arka.notification_service.application.usecases;

import com.arka.notification_service.application.dto.NotificationResponse;
import com.arka.notification_service.application.dto.SendNotificationRequest;
import com.arka.notification_service.domain.entities.Notification;
import com.arka.notification_service.domain.entities.NotificationStatus;
import com.arka.notification_service.domain.exceptions.EmailSendingException;
import com.arka.notification_service.domain.repositories.NotificationRepository;
import com.arka.notification_service.infrastructure.config.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public NotificationResponse execute(SendNotificationRequest request) {
        log.info("Sending notification of type: {} to: {}", request.getType(), request.getRecipientEmail());

        // Create notification entity
        Notification notification = Notification.builder()
                .type(request.getType())
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(NotificationStatus.PENDING)
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save notification as PENDING
        Notification savedNotification = notificationRepository.save(notification);

        // Try to send email
        try {
            emailService.sendEmail(
                    request.getRecipientEmail(),
                    request.getRecipientName(),
                    request.getSubject(),
                    request.getContent()
            );

            // Mark as SENT
            savedNotification.markAsSent();
            savedNotification = notificationRepository.save(savedNotification);

            log.info("Notification sent successfully: ID={}", savedNotification.getId());

        } catch (EmailSendingException e) {
            log.error("Failed to send notification: {}", e.getMessage());

            // Mark as FAILED
            savedNotification.markAsFailed(e.getMessage());
            savedNotification = notificationRepository.save(savedNotification);
        }

        return mapToResponse(savedNotification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .recipientEmail(notification.getRecipientEmail())
                .recipientName(notification.getRecipientName())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .orderId(notification.getOrderId())
                .orderNumber(notification.getOrderNumber())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
