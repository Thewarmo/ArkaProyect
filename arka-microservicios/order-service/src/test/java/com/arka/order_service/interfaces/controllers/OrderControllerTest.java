package com.arka.order_service.interfaces.controllers;

import com.arka.order_service.application.dto.*;
import com.arka.order_service.application.usecases.*;
import com.arka.order_service.domain.entities.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test de Controller de Órdenes
 *
 * Este test demuestra:
 * - Creación de órdenes con validación
 * - Consulta de órdenes por ID
 * - Cancelación de órdenes
 * - Validación de requests complejos con items
 * - Manejo de códigos de estado HTTP (201 CREATED, 200 OK)
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @MockBean
    private CancelOrderUseCase cancelOrderUseCase;

    private CreateOrderRequest createRequest;
    private OrderResponse orderResponse;
    private OrderItemDTO item1;
    private OrderItemDTO item2;

    @BeforeEach
    void setUp() {
        // Preparar items
        item1 = OrderItemDTO.builder()
                .id(1L)
                .productId(100L)
                .productName("Laptop HP")
                .quantity(1)
                .unitPrice(new BigDecimal("1500.00"))
                .subtotal(new BigDecimal("1500.00"))
                .build();

        item2 = OrderItemDTO.builder()
                .id(2L)
                .productId(200L)
                .productName("Mouse Logitech")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        // Preparar request de creación
        createRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList(item1, item2))
                .shippingAddress("Calle Falsa 123, Ciudad")
                .notes("Entrega en horas de oficina")
                .build();

        // Preparar response
        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-2024-00001")
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new BigDecimal("1600.00"))
                .totalQuantity(3)
                .shippingAddress("Calle Falsa 123, Ciudad")
                .notes("Entrega en horas de oficina")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given
        given(createOrderUseCase.execute(any(CreateOrderRequest.class)))
                .willReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-2024-00001"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(1600.00))
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.shippingAddress").value("Calle Falsa 123, Ciudad"));
    }

    @Test
    void shouldRejectCreateOrderWithoutCustomerId() throws Exception {
        // Given - Request sin customerId
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(null)
                .items(Arrays.asList(item1))
                .shippingAddress("Calle Falsa 123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateOrderWithEmptyItems() throws Exception {
        // Given - Request sin items
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList())
                .shippingAddress("Calle Falsa 123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateOrderWithBlankShippingAddress() throws Exception {
        // Given - Request sin dirección
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList(item1))
                .shippingAddress("  ")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateOrderWithInvalidItemQuantity() throws Exception {
        // Given - Item con cantidad inválida
        OrderItemDTO invalidItem = OrderItemDTO.builder()
                .productId(100L)
                .quantity(0) // Cantidad inválida
                .unitPrice(new BigDecimal("100.00"))
                .build();

        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList(invalidItem))
                .shippingAddress("Calle Falsa 123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateOrderWithInvalidItemPrice() throws Exception {
        // Given - Item con precio inválido
        OrderItemDTO invalidItem = OrderItemDTO.builder()
                .productId(100L)
                .quantity(1)
                .unitPrice(new BigDecimal("0.00")) // Precio inválido
                .build();

        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList(invalidItem))
                .shippingAddress("Calle Falsa 123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateOrderWithoutNotes() throws Exception {
        // Given - Request sin notas (campo opcional)
        CreateOrderRequest requestWithoutNotes = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList(item1))
                .shippingAddress("Calle Falsa 123")
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(2L)
                .orderNumber("ORD-2024-00002")
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .items(Arrays.asList(item1))
                .totalAmount(new BigDecimal("1500.00"))
                .totalQuantity(1)
                .shippingAddress("Calle Falsa 123")
                .createdAt(LocalDateTime.now())
                .build();

        given(createOrderUseCase.execute(any(CreateOrderRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutNotes)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-2024-00002"))
                .andExpect(jsonPath("$.notes").doesNotExist());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // Given
        given(getOrderByIdUseCase.execute(1L))
                .willReturn(orderResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-2024-00001"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(1600.00));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        // Given
        OrderResponse cancelledOrder = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-2024-00001")
                .customerId(1L)
                .status(OrderStatus.CANCELLED)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new BigDecimal("1600.00"))
                .totalQuantity(3)
                .shippingAddress("Calle Falsa 123, Ciudad")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(cancelOrderUseCase.execute(1L))
                .willReturn(cancelledOrder);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/cancel", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldCreateOrderWithMultipleItems() throws Exception {
        // Given - Orden con 5 items
        List<OrderItemDTO> multipleItems = Arrays.asList(
                item1, item2,
                OrderItemDTO.builder().productId(3L).quantity(1).unitPrice(new BigDecimal("200.00")).build(),
                OrderItemDTO.builder().productId(4L).quantity(3).unitPrice(new BigDecimal("50.00")).build(),
                OrderItemDTO.builder().productId(5L).quantity(2).unitPrice(new BigDecimal("100.00")).build()
        );

        CreateOrderRequest multiItemRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(multipleItems)
                .shippingAddress("Dirección de envío")
                .build();

        OrderResponse multiItemResponse = OrderResponse.builder()
                .id(3L)
                .orderNumber("ORD-2024-00003")
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .items(multipleItems)
                .totalAmount(new BigDecimal("2150.00"))
                .totalQuantity(9)
                .shippingAddress("Dirección de envío")
                .createdAt(LocalDateTime.now())
                .build();

        given(createOrderUseCase.execute(any(CreateOrderRequest.class)))
                .willReturn(multiItemResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(multiItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.totalQuantity").value(9))
                .andExpect(jsonPath("$.totalAmount").value(2150.00));
    }

    @Test
    void shouldCreateOrderWithLargeQuantity() throws Exception {
        // Given - Item con gran cantidad
        OrderItemDTO largeQuantityItem = OrderItemDTO.builder()
                .productId(100L)
                .productName("Producto Mayorista")
                .quantity(1000)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("10000.00"))
                .build();

        CreateOrderRequest largeRequest = CreateOrderRequest.builder()
                .customerId(5L)
                .items(Arrays.asList(largeQuantityItem))
                .shippingAddress("Almacén Central")
                .notes("Pedido mayorista")
                .build();

        OrderResponse largeResponse = OrderResponse.builder()
                .id(10L)
                .orderNumber("ORD-2024-10000")
                .customerId(5L)
                .status(OrderStatus.PENDING)
                .items(Arrays.asList(largeQuantityItem))
                .totalAmount(new BigDecimal("10000.00"))
                .totalQuantity(1000)
                .shippingAddress("Almacén Central")
                .notes("Pedido mayorista")
                .createdAt(LocalDateTime.now())
                .build();

        given(createOrderUseCase.execute(any(CreateOrderRequest.class)))
                .willReturn(largeResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(largeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalQuantity").value(1000))
                .andExpect(jsonPath("$.totalAmount").value(10000.00));
    }

    @Test
    void shouldHandleOrderWithSpecialCharactersInAddress() throws Exception {
        // Given - Dirección con caracteres especiales
        CreateOrderRequest specialCharsRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .items(Arrays.asList(item1))
                .shippingAddress("Calle Ñandú #123, Apto. 4º-B, Bogotá (Colombia)")
                .notes("Cliente: José García")
                .build();

        OrderResponse specialCharsResponse = OrderResponse.builder()
                .id(5L)
                .orderNumber("ORD-2024-00005")
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .items(Arrays.asList(item1))
                .totalAmount(new BigDecimal("1500.00"))
                .totalQuantity(1)
                .shippingAddress("Calle Ñandú #123, Apto. 4º-B, Bogotá (Colombia)")
                .notes("Cliente: José García")
                .createdAt(LocalDateTime.now())
                .build();

        given(createOrderUseCase.execute(any(CreateOrderRequest.class)))
                .willReturn(specialCharsResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialCharsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("Calle Ñandú #123, Apto. 4º-B, Bogotá (Colombia)"))
                .andExpect(jsonPath("$.notes").value("Cliente: José García"));
    }
}