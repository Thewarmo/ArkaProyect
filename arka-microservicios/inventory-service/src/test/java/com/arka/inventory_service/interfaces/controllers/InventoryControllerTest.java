package com.arka.inventory_service.interfaces.controllers;

import com.arka.inventory_service.application.dto.*;
import com.arka.inventory_service.application.usecases.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de Controller de Inventario
 *
 * Este test demuestra:
 * - Tests de endpoints de inventario
 * - Validación de requests de reserva y liberación
 * - Manejo de operaciones de stock
 * - Respuestas con información de versión (optimistic locking)
 */
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetInventoryByProductIdUseCase getInventoryByProductIdUseCase;

    @MockBean
    private CreateInventoryUseCase createInventoryUseCase;

    @MockBean
    private ReserveStockUseCase reserveStockUseCase;

    @MockBean
    private ReleaseStockUseCase releaseStockUseCase;

    private InventoryResponse inventoryResponse;
    private ReserveStockRequest reserveRequest;
    private ReleaseStockRequest releaseRequest;

    @BeforeEach
    void setUp() {
        // Preparar respuesta de inventario
        inventoryResponse = InventoryResponse.builder()
                .id(1L)
                .productId(100L)
                .availableStock(80)
                .reservedStock(20)
                .totalStock(100)
                .minimumStock(10)
                .lowStock(false)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Preparar request de reserva
        reserveRequest = ReserveStockRequest.builder()
                .productId(100L)
                .quantity(20)
                .orderId("ORD-2024-00001")
                .reason("Reserva para orden de compra")
                .build();

        // Preparar request de liberación
        releaseRequest = ReleaseStockRequest.builder()
                .productId(100L)
                .quantity(10)
                .build();
    }

    @Test
    void shouldGetInventoryByProductId() throws Exception {
        // Given
        given(getInventoryByProductIdUseCase.execute(100L))
                .willReturn(inventoryResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/product/{productId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.availableStock").value(80))
                .andExpect(jsonPath("$.reservedStock").value(20))
                .andExpect(jsonPath("$.totalStock").value(100))
                .andExpect(jsonPath("$.minimumStock").value(10))
                .andExpect(jsonPath("$.lowStock").value(false))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void shouldReserveStockSuccessfully() throws Exception {
        // Given
        InventoryResponse afterReserve = InventoryResponse.builder()
                .id(1L)
                .productId(100L)
                .availableStock(60)
                .reservedStock(40)
                .totalStock(100)
                .minimumStock(10)
                .lowStock(false)
                .version(2) // Versión incrementada
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(reserveStockUseCase.execute(any(ReserveStockRequest.class)))
                .willReturn(afterReserve);

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.availableStock").value(60))
                .andExpect(jsonPath("$.reservedStock").value(40))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void shouldRejectReserveRequestWithMissingProductId() throws Exception {
        // Given - Request sin productId
        ReserveStockRequest invalidRequest = ReserveStockRequest.builder()
                .productId(null) // Falta productId
                .quantity(20)
                .orderId("ORD-001")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectReserveRequestWithInvalidQuantity() throws Exception {
        // Given - Cantidad cero o negativa
        ReserveStockRequest invalidRequest = ReserveStockRequest.builder()
                .productId(100L)
                .quantity(0) // Cantidad inválida
                .orderId("ORD-001")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectReserveRequestWithBlankOrderId() throws Exception {
        // Given - OrderId en blanco
        ReserveStockRequest invalidRequest = ReserveStockRequest.builder()
                .productId(100L)
                .quantity(20)
                .orderId("  ") // OrderId en blanco
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReserveStockWithOptionalReason() throws Exception {
        // Given - Request sin reason (opcional)
        ReserveStockRequest requestWithoutReason = ReserveStockRequest.builder()
                .productId(100L)
                .quantity(15)
                .orderId("ORD-002")
                .build();

        InventoryResponse response = InventoryResponse.builder()
                .id(1L)
                .productId(100L)
                .availableStock(65)
                .reservedStock(35)
                .totalStock(100)
                .minimumStock(10)
                .lowStock(false)
                .version(2)
                .build();

        given(reserveStockUseCase.execute(any(ReserveStockRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutReason)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableStock").value(65))
                .andExpect(jsonPath("$.reservedStock").value(35));
    }

    @Test
    void shouldReleaseStockSuccessfully() throws Exception {
        // Given
        doNothing().when(releaseStockUseCase).execute(any(ReleaseStockRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(releaseRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectReleaseRequestWithMissingProductId() throws Exception {
        // Given - Request sin productId
        ReleaseStockRequest invalidRequest = ReleaseStockRequest.builder()
                .productId(null)
                .quantity(10)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectReleaseRequestWithInvalidQuantity() throws Exception {
        // Given - Cantidad inválida
        ReleaseStockRequest invalidRequest = ReleaseStockRequest.builder()
                .productId(100L)
                .quantity(0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleMultipleReserveOperations() throws Exception {
        // Given - Primera reserva
        InventoryResponse firstReserve = InventoryResponse.builder()
                .productId(100L)
                .availableStock(70)
                .reservedStock(30)
                .version(2)
                .build();

        // Segunda reserva
        InventoryResponse secondReserve = InventoryResponse.builder()
                .productId(100L)
                .availableStock(50)
                .reservedStock(50)
                .version(3)
                .build();

        given(reserveStockUseCase.execute(any(ReserveStockRequest.class)))
                .willReturn(firstReserve)
                .willReturn(secondReserve);

        // When & Then - Primera reserva
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));

        // Segunda reserva
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(3));
    }

    @Test
    void shouldGetInventoryWithLowStockFlag() throws Exception {
        // Given - Inventario con bajo stock
        InventoryResponse lowStockInventory = InventoryResponse.builder()
                .id(2L)
                .productId(200L)
                .availableStock(5)
                .reservedStock(3)
                .totalStock(8)
                .minimumStock(10)
                .lowStock(true) // Bajo stock
                .version(1)
                .build();

        given(getInventoryByProductIdUseCase.execute(200L))
                .willReturn(lowStockInventory);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/product/{productId}", 200L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(200))
                .andExpect(jsonPath("$.totalStock").value(8))
                .andExpect(jsonPath("$.minimumStock").value(10))
                .andExpect(jsonPath("$.lowStock").value(true));
    }

    @Test
    void shouldReserveStockWithLargeQuantity() throws Exception {
        // Given - Reserva de gran cantidad
        ReserveStockRequest largeQuantityRequest = ReserveStockRequest.builder()
                .productId(100L)
                .quantity(1000)
                .orderId("ORD-BULK-001")
                .reason("Pedido mayorista")
                .build();

        InventoryResponse response = InventoryResponse.builder()
                .productId(100L)
                .availableStock(0)
                .reservedStock(1000)
                .totalStock(1000)
                .version(2)
                .build();

        given(reserveStockUseCase.execute(any(ReserveStockRequest.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(largeQuantityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedStock").value(1000))
                .andExpect(jsonPath("$.availableStock").value(0));
    }

    @Test
    void shouldHandleReserveAndReleaseSequence() throws Exception {
        // Given - Reservar stock
        InventoryResponse afterReserve = InventoryResponse.builder()
                .productId(100L)
                .availableStock(70)
                .reservedStock(30)
                .version(2)
                .build();

        given(reserveStockUseCase.execute(any(ReserveStockRequest.class)))
                .willReturn(afterReserve);

        doNothing().when(releaseStockUseCase).execute(any(ReleaseStockRequest.class));

        // When & Then - Reservar
        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedStock").value(30));

        // Liberar
        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(releaseRequest)))
                .andExpect(status().isOk());
    }
}
