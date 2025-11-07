package com.arka.notification_service.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class EmailConfig {

    private From from = new From();
    private boolean enabled = true;

    @Data
    public static class From {
        private String email = "noreply@arka.com";
        private String name = "Arka - Distribuidora de PC";
    }
}
