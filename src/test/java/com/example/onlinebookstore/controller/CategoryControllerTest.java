package com.example.onlinebookstore.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.onlinebookstore.dto.category.CategoryDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CategoryControllerTest {
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
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create valid new category")
    void createCategory_ValidRequestDto_Success() throws Exception {
        // Given
        CategoryDto requestCategoryDto = createCategoryDto();

        CategoryDto expected = expectedCategoryDto(requestCategoryDto);

        String jsonRequest = objectMapper.writeValueAsString(requestCategoryDto);

        // When
        MvcResult result = mockMvc.perform(
                post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andReturn();

        // Then

        CategoryDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CategoryDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create invalid new category")
    void createCategory_InvalidRequestDto_ShouldReturnException() throws Exception {
        // Given
        CategoryDto requestCategoryDto = new CategoryDto(null, null, null);

        String jsonRequest = objectMapper.writeValueAsString(requestCategoryDto);

        // When
        MvcResult result = mockMvc.perform(
                post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get all categories valid")
    void getAll_ValidGivenCategoriesInDatabase_ShouldReturnAllCategories() throws Exception {
        // Given
        List<CategoryDto> expected = expectedListCategory();

        // When
        MvcResult result = mockMvc.perform(
                get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<CategoryDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<List<CategoryDto>>() {}
        );
        Assertions.assertEquals(expected, actual);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get category by id valid")
    void getCategory_ByValidId_ShouldReturnCategoryId() throws Exception {
        // Given
        Long id = 1L;

        CategoryDto expected = createCategoryDto();

        // When

        MvcResult result = mockMvc.perform(
                get("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), CategoryDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get by category id invalid")
    void getCategory_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 7L;

        // When
        MvcResult result = mockMvc.perform(get("/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can't find category with id " + id));
    }

    @Sql(
            scripts = "classpath:database/books/restore-category-after-update.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update category by valid id")
    void updateCategory_ByValidId_ShouldReturnUpdateCategory() throws Exception {
        // Given
        Long id = 2L;

        CategoryDto updateCategoryDto = new CategoryDto(
                2L, "UpdateCategory", "UpdateDescription");

        CategoryDto expected = expectedCategoryDto(updateCategoryDto);

        String jsonRequest = objectMapper.writeValueAsString(updateCategoryDto);

        // When
        MvcResult result = mockMvc.perform(put("/categories/{id}", id)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CategoryDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CategoryDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update category by invalid id")
    void updateCategory_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 300L;

        CategoryDto invalidUpdate = new CategoryDto(null, "Category3", null);

        String jsonRequest = objectMapper.writeValueAsString(invalidUpdate);

        // When
        MvcResult result = mockMvc.perform(put("/categories/{id}", id)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Category with id " + id + " was not found"));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete category by id valid")
    void deleteCategory_ByValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long id = 2L;

        // When
        MvcResult result = mockMvc.perform(delete("/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete category by invalid id should return Exception")
    void deleteCategory_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 300L;

        // When
        MvcResult result = mockMvc.perform(delete("/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find category by id " + id));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get all books category id valid")
    void getAllBooks_ByValidCategoryId_ShouldReturnAllBooksWithoutCategoriesIds() throws Exception {
        // Given
        Long id = 1L;

        List<BookDtoWithoutCategoryIds> expected = createExpectedBookList();

        // When
        MvcResult result = mockMvc.perform(get("/categories/{id}/books", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDtoWithoutCategoryIds> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<List<BookDtoWithoutCategoryIds>>() {
                });
        Assertions.assertEquals(expected, actual);

    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get all books invalid category id")
    void getAllBooks_InByValidCategoryId_ShouldReturnException() throws Exception {
        // Given
        Long id = 99L;

        // When
        MvcResult result = mockMvc.perform(get("/categories/{id}/books", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then

        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find category by category id " + id));
    }

    private List<BookDtoWithoutCategoryIds> createExpectedBookList() {
        BookDtoWithoutCategoryIds book1 = new BookDtoWithoutCategoryIds();
        book1.setId(1L);
        book1.setTitle("Book1");
        book1.setAuthor("Author1");
        book1.setIsbn("1111111");
        book1.setPrice(BigDecimal.valueOf(10));
        book1.setDescription("Description1");
        book1.setCoverImage("CoverImage1");

        BookDtoWithoutCategoryIds book2 = new BookDtoWithoutCategoryIds();
        book2.setId(2L);
        book2.setTitle("Book2");
        book2.setAuthor("Author2");
        book2.setIsbn("2222222");
        book2.setPrice(BigDecimal.valueOf(15));
        book2.setDescription("Description2");
        book2.setCoverImage("CoverImage2");

        BookDtoWithoutCategoryIds book3 = new BookDtoWithoutCategoryIds();
        book3.setId(3L);
        book3.setTitle("Book3");
        book3.setAuthor("Author3");
        book3.setIsbn("3333333");
        book3.setPrice(BigDecimal.valueOf(20));
        book3.setDescription("Description3");
        book3.setCoverImage("CoverImage3");

        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();
        expected.add(book1);
        expected.add(book2);
        expected.add(book3);

        return expected;
    }

    private List<CategoryDto> expectedListCategory() {
        List<CategoryDto> expectedListCategory = new ArrayList<>();
        expectedListCategory.add(new CategoryDto(1L, "Category1", "Description1"));
        expectedListCategory.add(new CategoryDto(2L, "Category2", "Description2"));
        return expectedListCategory;
    }

    private CategoryDto createCategoryDto(
    ) {
        return new CategoryDto(1L,"Category1", "Description1");
    }

    private CategoryDto expectedCategoryDto(CategoryDto responseCategoryDto) {
        return new CategoryDto(
                responseCategoryDto.id(),
                responseCategoryDto.name(),
                responseCategoryDto.description());
    }
}
