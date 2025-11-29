package com.arka.order_service;

import com.arka.order_service.config.TestRabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
