package com.example.onlinebookstore.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.cartitem.CartItemDto;
import com.example.onlinebookstore.dto.cartitem.CartItemResponseDto;
import com.example.onlinebookstore.dto.cartitem.CartItemUpdateRequestDto;
import com.example.onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/insert-two-default-category.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/insert-three-default-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/add-book-category.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/insert-user.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/insert-shopping-cart.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/insert-cart-items.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/delete-book-category.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/remove-all-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/delete-all-category.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/remove-shopping-carts.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/remove-cart-items.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/remove-user.sql")
            );
        }
    }

    @DisplayName("Add book to shopping cart should return success")
    @WithUserDetails("user1@example.com")
    @Test
    void addBookToShopCart_ValidShoppingCartDto_ShouldReturnSuccess() throws Exception {
        // Given
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setBookId(2L);
        cartItemDto.setQuantity(1);
        ShoppingCartDto expected = new ShoppingCartDto(1L, 3L,
                Set.of(new CartItemResponseDto(1L, 1L, "Book1", 1),
                        new CartItemResponseDto(2L, 2L, "Book2", 1)));

        String jsonRequest = objectMapper.writeValueAsString(cartItemDto);

        // When
        MvcResult result = mockMvc.perform(post("/cart")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        ShoppingCartDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @DisplayName("Get shopping cart user")
    @WithUserDetails("user1@example.com")
    @Test
    void getShoppingCart_ByUserValid_ShouldReturnShoppingCartUser() throws Exception {
        // Given
        Long id = 3L;
        ShoppingCartDto expected = new ShoppingCartDto(1L, 3L,
                Set.of(new CartItemResponseDto(1L, 1L, "Book1", 1)));

        // When
        MvcResult result = mockMvc.perform(get("/cart", id)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ShoppingCartDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), ShoppingCartDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @DisplayName("update quantity by valid id from cart item")
    @WithUserDetails("user1@example.com")
    @Test
    void updateQuantity_ByValidId_ShouldReturnSuccess() throws Exception {
        // Given
        Long id = 1L;
        ShoppingCartDto expected = new ShoppingCartDto(1L, 3L,
                Set.of(new CartItemResponseDto(1L, 1L, "Book1", 1)));

        CartItemUpdateRequestDto updateRequestDto = new CartItemUpdateRequestDto(1);

        String jsonRequest = objectMapper.writeValueAsString(updateRequestDto);
        // When
        MvcResult result = mockMvc.perform(put("/cart/cart-items/{id}", id)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        ShoppingCartDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), ShoppingCartDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @DisplayName("Delete item from shopping cart")
    @WithUserDetails("user1@example.com")
    @Test
    void deleteItem_ByValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long id = 1L;

        // When
        MvcResult result = mockMvc.perform(delete("/cart/cart-items/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @DisplayName("Delete item from shopping cart by invalid id")
    @WithUserDetails("user1@example.com")
    @Test
    void deleteItem_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 100L;

        // When
        MvcResult result = mockMvc.perform(delete("/cart/cart-items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find item by id " + id));
    }
}
