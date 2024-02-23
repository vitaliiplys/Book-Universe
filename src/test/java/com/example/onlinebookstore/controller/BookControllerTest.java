package com.example.onlinebookstore.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.book.BookDto;
import com.example.onlinebookstore.dto.book.BookSearchParameters;
import com.example.onlinebookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
class BookControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach(
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

    @AfterEach
    void afterEach(
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
    @Sql(
            scripts = "classpath:database/books/delete-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create a new book")
    void createBook_ValidRequestDto_Success() throws Exception {
        // Given
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Book")
                .setAuthor("New Book")
                .setIsbn("12345678")
                .setPrice(BigDecimal.valueOf(15.99))
                .setDescription("Description")
                .setCoverImage("CoverImage")
                .setCategoriesIds(List.of(1L));

        BookDto expected = createExpectedBookDto(requestDto);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)

                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then

        BookDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new book invalid")
    void createBook_InvalidRequestDto_ShouldReturnException() throws Exception {
        // Given
        CreateBookRequestDto requestDto = createBookRequestDto()
                .setCategoriesIds(Collections.emptyList());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(post("/books")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Cant find categories by Empty ids list"));

    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get all books")
    void getAll_ValidGivenBooksInDatabase_ShouldReturnAllBooks() throws Exception {
        // Given
        List<BookDto> expected = createExpectedBookList();

        // When
        MvcResult result = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                new TypeReference<List<BookDto>>() {
                });
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get book by id")
    void getBook_ByValidId_ShouldReturnIdBook() throws Exception {
        // Given
        Long bookId = 1L;

        BookDto expectedBook = createBookDto(bookId, "Book1", "Author1", "1111111",
                BigDecimal.valueOf(10), "Description1", "CoverImage1", List.of(1L));

        //When
        MvcResult result = mockMvc.perform(get("/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then

        BookDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expectedBook, actual));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get book by invalid id")
    void getBook_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long bookInvalidId = 100L;

        // When
        MvcResult result = mockMvc.perform(get("/books/{bookInvalidId}", bookInvalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find book by id " + bookInvalidId));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Delete book by valid id")
    void deleteBook_ByValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long bookId = 3L;

        // When
        MvcResult result = mockMvc.perform(delete("/books/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Delete book by invalid id should return Exception")
    void deleteBook_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long bookId = 7L;

        // When
        MvcResult result = mockMvc.perform(delete("/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find book by id " + bookId));
    }

    @Sql(
            scripts = "classpath:database/books/restore-book-after-update.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Update book by id")
    void updateBook_ByValidId_ShouldReturnUpdateBook() throws Exception {
        //Given
        Long bookId = 1L;

        CreateBookRequestDto updateBookRequestDtoExpected = createBookRequestDto();

        BookDto expectedBookDto = createExpectedBookDto(updateBookRequestDtoExpected);
        expectedBookDto.setId(bookId);

        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDtoExpected);

        // When
        MvcResult result = mockMvc.perform(put("/books/{bookId}", bookId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then

        BookDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto.class);
        assertTrue(EqualsBuilder.reflectionEquals(expectedBookDto, actual));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Update book with invalid id")
    void updateBook_WithInvalidById_ShouldReturnException() throws Exception {
        // Given
        Long bookId = 500L;

        CreateBookRequestDto invalidUpdateRequestDto = createBookRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(invalidUpdateRequestDto);

        // When
        MvcResult result = mockMvc.perform(put("/books/{bookId}", bookId)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Can`t find book by id " + bookId));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Search book for parameters(title, author)")
    void searchBook_ByValidParameters_ShouldReturnParameters() throws Exception {
        // Given
        BookSearchParameters bookSearchParameters = new BookSearchParameters(
                new String[]{"Book1"},
                new String[]{"Author1"}
        );

        List<BookDto> expected = new ArrayList<>();
        expected.add(createBookDto(1L, "Book1", "Author1",
                "1111111", BigDecimal.valueOf(10),
                "Description1", "CoverImage1", List.of(1L)));
        // When

        MvcResult result = mockMvc.perform(get("/books/search")
                        .param("titles",bookSearchParameters.titles())
                        .param("authors", bookSearchParameters.authors())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // Then

        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                new TypeReference<List<BookDto>>() {
                });
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Search book for invalid parameters(title, author)")
    void searchBook_ByInvalidParameters_ShouldReturnException() throws Exception {
        // Given
        BookSearchParameters bookSearchParameters = new BookSearchParameters(
                new String[]{"Book7"},
                new String[]{"Author7"}
        );

        // When

        MvcResult result = mockMvc.perform(get("/books/search")
                        .param("titles",bookSearchParameters.titles())
                        .param("authors", bookSearchParameters.authors())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // Then

        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                new TypeReference<List<BookDto>>() {
                });
        Assertions.assertTrue(actual.isEmpty());
    }

    private List<BookDto> createExpectedBookList() {
        List<BookDto> expected = new ArrayList<>();
        expected.add(createBookDto(1L, "Book1", "Author1",
                "1111111", BigDecimal.valueOf(10),
                "Description1", "CoverImage1", List.of(1L)));

        expected.add(createBookDto(2L, "Book2", "Author2",
                "2222222", BigDecimal.valueOf(15),
                "Description2", "CoverImage2", List.of(1L)));

        expected.add(createBookDto(3L, "Book3", "Author3",
                "3333333", BigDecimal.valueOf(20),
                "Description3", "CoverImage3", List.of(1L)));
        return expected;
    }

    private BookDto createBookDto(
            Long id, String title, String author, String isbn,
            BigDecimal price, String description, String coverImage, List<Long> categoriesIds) {
        return new BookDto()
                .setId(id)
                .setTitle(title)
                .setAuthor(author)
                .setIsbn(isbn)
                .setPrice(price)
                .setDescription(description)
                .setCoverImage(coverImage)
                .setCategoriesIds(categoriesIds);
    }

    private BookDto createExpectedBookDto(CreateBookRequestDto requestDto) {
        return new BookDto()
                .setTitle(requestDto.getTitle())
                .setAuthor(requestDto.getAuthor())
                .setIsbn(requestDto.getIsbn())
                .setPrice(requestDto.getPrice())
                .setDescription(requestDto.getDescription())
                .setCoverImage(requestDto.getCoverImage())
                .setCategoriesIds(requestDto.getCategoriesIds());
    }

    private CreateBookRequestDto createBookRequestDto(
    ) {
        return new CreateBookRequestDto()
                .setTitle("Update Book")
                .setAuthor("Update Author")
                .setIsbn("1111111 ")
                .setPrice(BigDecimal.valueOf(13))
                .setDescription("Update Description")
                .setCoverImage("Update CoverImage")
                .setCategoriesIds(List.of(1L));
    }
}
