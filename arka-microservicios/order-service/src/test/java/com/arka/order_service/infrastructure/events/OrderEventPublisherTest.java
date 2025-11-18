package com.arka.order_service.infrastructure.events;

import com.arka.order_service.infrastructure.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Test de Integración de RabbitMQ usando Testcontainers
 *
 * Este test demuestra:
 * - Configuración de RabbitMQ con Testcontainers
 * - Publicación de mensajes con RabbitTemplate
 * - Verificación de mensajes en las colas
 * - Deserialización de mensajes JSON
 * - Uso de Awaitility para operaciones asíncronas
 */
@SpringBootTest
@Testcontainers
class OrderEventPublisherTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-alpine")
            .withExposedPorts(5672);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderCreatedEvent testEvent;

    @BeforeEach
    void setUp() {
        // Limpiar colas antes de cada test
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(RabbitMQConfig.ORDER_CREATED_QUEUE);
            return null;
        });

        // Crear evento de prueba
        testEvent = OrderCreatedEvent.builder()
                .orderId(1L)
                .orderNumber("ORD-2024-00001")
                .customerId(100L)
                .customerEmail("customer@example.com")
                .customerName("Juan Pérez")
                .totalAmount(new BigDecimal("1500.00"))
                .shippingAddress("Calle Falsa 123, Ciudad")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldPublishOrderCreatedEvent() throws Exception {
        // When
        orderEventPublisher.publishOrderCreatedEvent(testEvent);

        // Then - Verificar que el mensaje llegó a la cola
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message).isNotNull();

                    // Deserializar el mensaje
                    OrderCreatedEvent receivedEvent = objectMapper.readValue(
                            message.getBody(),
                            OrderCreatedEvent.class
                    );

                    // Verificar contenido del evento
                    assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
                    assertThat(receivedEvent.getOrderNumber()).isEqualTo("ORD-2024-00001");
                    assertThat(receivedEvent.getCustomerId()).isEqualTo(100L);
                    assertThat(receivedEvent.getCustomerEmail()).isEqualTo("customer@example.com");
                    assertThat(receivedEvent.getCustomerName()).isEqualTo("Juan Pérez");
                    assertThat(receivedEvent.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
                    assertThat(receivedEvent.getShippingAddress()).isEqualTo("Calle Falsa 123, Ciudad");
                    assertThat(receivedEvent.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void shouldPublishMultipleOrderCreatedEvents() throws Exception {
        // Given - Múltiples eventos
        OrderCreatedEvent event1 = OrderCreatedEvent.builder()
                .orderId(1L)
                .orderNumber("ORD-001")
                .customerId(100L)
                .customerEmail("customer1@example.com")
                .customerName("Cliente 1")
                .totalAmount(new BigDecimal("1000.00"))
                .shippingAddress("Dirección 1")
                .createdAt(LocalDateTime.now())
                .build();

        OrderCreatedEvent event2 = OrderCreatedEvent.builder()
                .orderId(2L)
                .orderNumber("ORD-002")
                .customerId(200L)
                .customerEmail("customer2@example.com")
                .customerName("Cliente 2")
                .totalAmount(new BigDecimal("2000.00"))
                .shippingAddress("Dirección 2")
                .createdAt(LocalDateTime.now())
                .build();

        // When
        orderEventPublisher.publishOrderCreatedEvent(event1);
        orderEventPublisher.publishOrderCreatedEvent(event2);

        // Then - Verificar que ambos mensajes llegaron
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Recibir primer mensaje
                    Message message1 = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message1).isNotNull();

                    OrderCreatedEvent received1 = objectMapper.readValue(
                            message1.getBody(),
                            OrderCreatedEvent.class
                    );
                    assertThat(received1.getOrderNumber()).isEqualTo("ORD-001");

                    // Recibir segundo mensaje
                    Message message2 = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message2).isNotNull();

                    OrderCreatedEvent received2 = objectMapper.readValue(
                            message2.getBody(),
                            OrderCreatedEvent.class
                    );
                    assertThat(received2.getOrderNumber()).isEqualTo("ORD-002");
                });
    }

    @Test
    void shouldPublishEventToCorrectExchangeAndRoutingKey() throws Exception {
        // When
        orderEventPublisher.publishOrderCreatedEvent(testEvent);

        // Then - Verificar que el mensaje está en la cola correcta
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message).isNotNull();

                    // Verificar propiedades del mensaje
                    assertThat(message.getMessageProperties().getReceivedExchange())
                            .isEqualTo(RabbitMQConfig.ORDER_EXCHANGE);
                    assertThat(message.getMessageProperties().getReceivedRoutingKey())
                            .isEqualTo(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY);
                    assertThat(message.getMessageProperties().getContentType())
                            .isEqualTo("application/json");
                });
    }

    @Test
    void shouldHandleEventWithMinimalData() throws Exception {
        // Given - Evento con datos mínimos
        OrderCreatedEvent minimalEvent = OrderCreatedEvent.builder()
                .orderId(999L)
                .orderNumber("ORD-MINIMAL")
                .customerId(1L)
                .customerEmail("minimal@example.com")
                .customerName("Minimal Customer")
                .totalAmount(new BigDecimal("10.00"))
                .shippingAddress("Address")
                .createdAt(LocalDateTime.now())
                .build();

        // When
        orderEventPublisher.publishOrderCreatedEvent(minimalEvent);

        // Then
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message).isNotNull();

                    OrderCreatedEvent receivedEvent = objectMapper.readValue(
                            message.getBody(),
                            OrderCreatedEvent.class
                    );

                    assertThat(receivedEvent.getOrderId()).isEqualTo(999L);
                    assertThat(receivedEvent.getOrderNumber()).isEqualTo("ORD-MINIMAL");
                    assertThat(receivedEvent.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
                });
    }

    @Test
    void shouldHandleEventWithLargeAmount() throws Exception {
        // Given - Evento con monto grande
        OrderCreatedEvent largeAmountEvent = OrderCreatedEvent.builder()
                .orderId(100L)
                .orderNumber("ORD-LARGE")
                .customerId(50L)
                .customerEmail("large@example.com")
                .customerName("Large Order Customer")
                .totalAmount(new BigDecimal("999999.99"))
                .shippingAddress("Large Address")
                .createdAt(LocalDateTime.now())
                .build();

        // When
        orderEventPublisher.publishOrderCreatedEvent(largeAmountEvent);

        // Then
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message).isNotNull();

                    OrderCreatedEvent receivedEvent = objectMapper.readValue(
                            message.getBody(),
                            OrderCreatedEvent.class
                    );

                    assertThat(receivedEvent.getTotalAmount())
                            .isEqualByComparingTo(new BigDecimal("999999.99"));
                });
    }

    @Test
    void shouldNotBlockWhenPublishingEvent() {
        // When - Publicar evento de forma síncrona
        long startTime = System.currentTimeMillis();
        orderEventPublisher.publishOrderCreatedEvent(testEvent);
        long endTime = System.currentTimeMillis();

        // Then - La publicación debe ser muy rápida (< 1 segundo)
        long duration = endTime - startTime;
        assertThat(duration).isLessThan(1000L);
    }

    @Test
    void shouldPublishEventWithSpecialCharactersInAddress() throws Exception {
        // Given - Evento con caracteres especiales
        OrderCreatedEvent specialCharsEvent = OrderCreatedEvent.builder()
                .orderId(200L)
                .orderNumber("ORD-SPECIAL")
                .customerId(300L)
                .customerEmail("special@example.com")
                .customerName("José García Ñoño")
                .totalAmount(new BigDecimal("500.00"))
                .shippingAddress("Calle Ñandú #123, Apto. 4º-B, Bogotá (Colombia)")
                .createdAt(LocalDateTime.now())
                .build();

        // When
        orderEventPublisher.publishOrderCreatedEvent(specialCharsEvent);

        // Then
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Message message = rabbitTemplate.receive(RabbitMQConfig.ORDER_CREATED_QUEUE, 1000);
                    assertThat(message).isNotNull();

                    OrderCreatedEvent receivedEvent = objectMapper.readValue(
                            message.getBody(),
                            OrderCreatedEvent.class
                    );

                    assertThat(receivedEvent.getCustomerName()).isEqualTo("José García Ñoño");
                    assertThat(receivedEvent.getShippingAddress())
                            .isEqualTo("Calle Ñandú #123, Apto. 4º-B, Bogotá (Colombia)");
                });
    }
}