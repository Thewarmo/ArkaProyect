package com.arka.notification_service;

import com.arka.notification_service.config.TestRabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
