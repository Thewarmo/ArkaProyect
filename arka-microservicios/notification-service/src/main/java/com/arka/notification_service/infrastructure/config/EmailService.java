package com.arka.notification_service.infrastructure.config;

import com.arka.notification_service.domain.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    public void sendEmail(String to, String toName, String subject, String content) {
        if (!emailConfig.isEnabled()) {
            log.warn("Email sending is disabled. Skipping email to: {}", to);
            return;
        }

        try {
            log.info("Preparing to send email to: {}", to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom().getEmail(), emailConfig.getFrom().getName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML content

            mailSender.send(message);

            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
            throw new EmailSendingException("Failed to send email to: " + to, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}. Error: {}", to, e.getMessage());
            throw new EmailSendingException("Unexpected error sending email", e);
        }
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        String htmlContent = String.format(
                "<html><body><p>%s</p></body></html>",
                body.replace("\n", "<br>")
        );
        sendEmail(to, null, subject, htmlContent);
    }
}
