package com.arka.notification_service.application.dto;

import com.arka.notification_service.domain.entities.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    private String recipientName;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    // Optional: Reference to related entity
    private Long orderId;
    private String orderNumber;
}
