package com.arka.product_service.interfaces.controllers;

import com.arka.product_service.application.dto.*;
import com.arka.product_service.application.usecases.*;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test de Controlador usando @WebMvcTest
 *
 * Este test demuestra:
 * - Configuración de tests de controladores con MockMvc
 * - Mockeo de casos de uso con @MockBean
 * - Validación de requests y responses JSON
 * - Verificación de códigos de estado HTTP
 * - Validación de entrada con Bean Validation
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProductUseCase createProductUseCase;

    @MockBean
    private GetAllProductsUseCase getAllProductsUseCase;

    @MockBean
    private GetProductByIdUseCase getProductByIdUseCase;

    @MockBean
    private UpdateProductStockUseCase updateProductStockUseCase;

    @MockBean
    private GetLowStockProductsUseCase getLowStockProductsUseCase;

    private ProductResponse productResponse;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Laptop HP");
        productResponse.setDescription("Laptop para desarrollo");
        productResponse.setPrice(new BigDecimal("1500.00"));
        productResponse.setStock(10);
        productResponse.setCategoryId(1L);
        productResponse.setBrandId(1L);
        productResponse.setCreatedAt(LocalDateTime.now());
        productResponse.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateProductRequest();
        createRequest.setName("Laptop HP");
        createRequest.setDescription("Laptop para desarrollo");
        createRequest.setPrice(new BigDecimal("1500.00"));
        createRequest.setStock(10);
        createRequest.setCategoryId(1L);
        createRequest.setBrandId(1L);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        // Given
        given(createProductUseCase.execute(any(CreateProductRequest.class)))
                .willReturn(productResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop HP"))
                .andExpect(jsonPath("$.price").value(1500.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.brandId").value(1));
    }

    @Test
    void shouldRejectCreateProductWithInvalidData() throws Exception {
        // Given - Request con datos inválidos
        CreateProductRequest invalidRequest = new CreateProductRequest();
        invalidRequest.setName("AB"); // Muy corto (mínimo 3)
        invalidRequest.setPrice(new BigDecimal("0.00")); // Precio inválido
        invalidRequest.setStock(-5); // Stock negativo
        // categoryId faltante (requerido)

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateProductWithBlankName() throws Exception {
        // Given
        createRequest.setName("  "); // Nombre en blanco

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateProductWithNegativePrice() throws Exception {
        // Given
        createRequest.setPrice(new BigDecimal("-100.00"));

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Given
        ProductResponse product2 = new ProductResponse();
        product2.setId(2L);
        product2.setName("Mouse Logitech");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(20);
        product2.setCategoryId(2L);

        List<ProductResponse> products = Arrays.asList(productResponse, product2);
        given(getAllProductsUseCase.execute()).willReturn(products);

        // When & Then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Laptop HP"))
                .andExpect(jsonPath("$[1].name").value("Mouse Logitech"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        // Given
        given(getProductByIdUseCase.execute(1L))
                .willReturn(Optional.of(productResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop HP"))
                .andExpect(jsonPath("$.price").value(1500.00));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        // Given
        given(getProductByIdUseCase.execute(999L))
                .willReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateProductStock() throws Exception {
        // Given
        UpdateStockRequest updateRequest = new UpdateStockRequest();
        updateRequest.setNewStock(15);
        updateRequest.setReason("Reabastecimiento");

        ProductResponse updatedProduct = new ProductResponse();
        updatedProduct.setId(1L);
        updatedProduct.setName("Laptop HP");
        updatedProduct.setStock(15);
        updatedProduct.setPrice(new BigDecimal("1500.00"));
        updatedProduct.setCategoryId(1L);

        given(updateProductStockUseCase.execute(eq(1L), any(UpdateStockRequest.class)))
                .willReturn(Optional.of(updatedProduct));

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}/stock", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStockOfNonExistentProduct() throws Exception {
        // Given
        UpdateStockRequest updateRequest = new UpdateStockRequest();
        updateRequest.setNewStock(5);

        given(updateProductStockUseCase.execute(eq(999L), any(UpdateStockRequest.class)))
                .willReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}/stock", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectStockUpdateWithNegativeQuantity() throws Exception {
        // Given
        UpdateStockRequest invalidRequest = new UpdateStockRequest();
        invalidRequest.setNewStock(-5); // Stock negativo (inválido)

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}/stock", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetLowStockProducts() throws Exception {
        // Given
        ProductResponse lowStockProduct = new ProductResponse();
        lowStockProduct.setId(1L);
        lowStockProduct.setName("Laptop HP");
        lowStockProduct.setStock(5);
        lowStockProduct.setPrice(new BigDecimal("1500.00"));
        lowStockProduct.setCategoryId(1L);

        LowStockReportResponse reportResponse = new LowStockReportResponse();
        reportResponse.setThreshold(10);
        reportResponse.setTotalProductsFound(1);
        reportResponse.setLowStockProducts(Arrays.asList(lowStockProduct));

        given(getLowStockProductsUseCase.execute(any(LowStockReportRequest.class)))
                .willReturn(reportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threshold").value(10))
                .andExpect(jsonPath("$.totalProductsFound").value(1))
                .andExpect(jsonPath("$.lowStockProducts", hasSize(1)))
                .andExpect(jsonPath("$.lowStockProducts[0].name").value("Laptop HP"))
                .andExpect(jsonPath("$.lowStockProducts[0].stock").value(5));
    }

    @Test
    void shouldGetLowStockProductsWithFilters() throws Exception {
        // Given
        LowStockReportResponse reportResponse = new LowStockReportResponse();
        reportResponse.setThreshold(10);
        reportResponse.setTotalProductsFound(0);
        reportResponse.setLowStockProducts(Arrays.asList());

        given(getLowStockProductsUseCase.execute(any(LowStockReportRequest.class)))
                .willReturn(reportResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products/low-stock")
                        .param("threshold", "10")
                        .param("categoryId", "1")
                        .param("brandId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threshold").value(10))
                .andExpect(jsonPath("$.totalProductsFound").value(0));
    }

    @Test
    void shouldUseDefaultThresholdWhenNotProvided() throws Exception {
        // Given
        LowStockReportResponse reportResponse = new LowStockReportResponse();
        reportResponse.setThreshold(10); // Default value
        reportResponse.setTotalProductsFound(0);
        reportResponse.setLowStockProducts(Arrays.asList());

        given(getLowStockProductsUseCase.execute(any(LowStockReportRequest.class)))
                .willReturn(reportResponse);

        // When & Then - No se proporciona threshold, debe usar el default (10)
        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threshold").value(10));
    }
}