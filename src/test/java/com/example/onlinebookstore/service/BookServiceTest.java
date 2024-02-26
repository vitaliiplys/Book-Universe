package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.book.BookDto;
import com.example.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.onlinebookstore.dto.book.BookSearchParameters;
import com.example.onlinebookstore.dto.book.CreateBookRequestDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.BookMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.book.BookSpecificationBuilder;
import com.example.onlinebookstore.repository.category.CategoryRepository;
import com.example.onlinebookstore.service.book.impl.BookServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @DisplayName("Get book by valid id")
    @Test
    void getBookById_ValidBookId_ShouldReturnBookId() {
        // Given
        Long id = 1L;

        Book book = createBook();
        BookDto bookDto = createBookDto();

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        // When
        BookDto result = bookService.findById(id);

        // Then
        assertEquals(result, bookDto);
        verify(bookRepository, times(1)).findById(id);
        verify(bookMapper, times(1)).toDto(book);
    }

    @DisplayName("Get book by invalid id")
    @Test
    void getBookById_InvalidBookId_ShouldReturnException() {
        // Given
        Long invalidId = 100L;

        when(bookRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.findById(invalidId)
        );

        // Then
        String expected = "Can`t find book by id " + invalidId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(invalidId);
    }

    @DisplayName("Update book by valid id")
    @Test
    void updateBookId_ValidBook_ShouldReturnBookUpdate() {
        // Given
        Long bookId = 2L;

        Book existingBook = createBook();
        existingBook.setId(bookId);

        CreateBookRequestDto updateBookRequestDto = createBookRequestDto();
        updateBookRequestDto.setAuthor("Update Author1");

        Book updateBook = createBook();
        updateBook.setAuthor("Update Author1");

        BookDto bookDto = createBookDto();
        bookDto.setAuthor("Update Author1");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(bookMapper.toModel(updateBookRequestDto)).thenReturn(updateBook);
        when(bookRepository.save(updateBook)).thenReturn(updateBook);
        when(bookMapper.toDto(updateBook)).thenReturn(bookDto);

        // When
        BookDto result = bookService.updateById(bookId, updateBookRequestDto);

        // Then
        assertThat(result).isEqualTo(bookDto);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1)).toModel(updateBookRequestDto);
        verify(bookRepository, times(1)).save(updateBook);
        verify(categoryRepository, times(1)).findById(category().getId());
        verify(bookMapper, times(1)).toDto(updateBook);
    }

    @DisplayName("Update book by invalid id")
    @Test
    void updateBookId_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 99L;
        CreateBookRequestDto bookRequestDto = createBookRequestDto();

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.updateById(id, bookRequestDto)
        );

        // Then
        String expected = "Can`t find book by id " + id;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(id);
    }

    @DisplayName("Create a new book ")
    @Test
    void saveBook_ValidBook_ReturnBookRequestDtoOk() {
        // Given
        CreateBookRequestDto requestDto = createBookRequestDto();

        Book book = createBook();

        Book savedBook = createBook();
        savedBook.setId(1L);

        BookDto bookDto = createBookDto();

        when(bookMapper.toModel(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(savedBook);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(bookMapper.toDto(savedBook)).thenReturn(bookDto);

        // When
        BookDto result = bookService.save(requestDto);

        // Then
        Assertions.assertEquals(bookDto, result);
        verify(bookMapper, times(1)).toDto(savedBook);
        verify(bookMapper, times(1)).toModel(requestDto);
        verify(bookRepository, times(1)).save(book);
        verify(categoryRepository, times(1)).findById(category().getId());
    }

    @DisplayName("Save book with empty categories")
    @Test
    void saveBook_WithEmptyCategoriesIds_ShouldReturnException() {
        // Given
        CreateBookRequestDto requestDto = createBookRequestDto();
        requestDto.setCategoriesIds(Collections.emptyList());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.save(requestDto)
        );

        // Then
        String expected = "Cant find categories by Empty ids list";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @DisplayName("Find all book valid test")
    @Test
    void findAll_ValidPageable_ReturnAllBooksOk() {
        // Given
        List<Book> bookList = createBookList();
        List<BookDto> expectedDto = createBookDtoList();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(bookList, pageable, bookList.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(bookList.get(0))).thenReturn(expectedDto.get(0));
        when(bookMapper.toDto(bookList.get(1))).thenReturn(expectedDto.get(1));

        // When
        List<BookDto> result = bookService.findAll(pageable);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(expectedDto.get(0));
        assertThat(result.get(1)).isEqualTo(expectedDto.get(1));
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDto(bookList.get(0));
        verify(bookMapper, times(1)).toDto(bookList.get(1));
    }

    @DisplayName("Find all by category valid id")
    @Test
    void findAllByCategoryId_ValidByCategoryId_ShouldReturnBookDtoWithoutCategoryIds() {
        // Given
        Long categoryId = 1L;
        List<Book> bookList = createBookList();
        List<BookDtoWithoutCategoryIds> expected = withoutCategoryIds();

        when(bookRepository.existsById(categoryId)).thenReturn(true);
        when(bookMapper.toDtoWithoutCategories(bookList.get(0))).thenReturn(expected.get(0));
        when(bookMapper.toDtoWithoutCategories(bookList.get(1))).thenReturn(expected.get(1));
        when(bookRepository.findAllByCategoriesId(categoryId)).thenReturn(bookList);

        // When
        List<BookDtoWithoutCategoryIds> result = bookService.findAllByCategoryId(categoryId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(expected.get(0));
        assertThat(result.get(1)).isEqualTo(expected.get(1));
        verify(bookMapper, times(1)).toDtoWithoutCategories(bookList.get(0));
        verify(bookMapper, times(1)).toDtoWithoutCategories(bookList.get(1));
        verify(bookRepository, times(1)).existsById(categoryId);
        verify(bookRepository, times(1)).findAllByCategoriesId(categoryId);
    }

    @DisplayName("Find all by category invalid id")
    @Test
    void findAllByCategoryId_InvalidByCategoryId_ShouldReturnException() {
        // Given
        Long invalidCategory = 1L;

        when(bookRepository.existsById(invalidCategory)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.findAllByCategoryId(invalidCategory)
        );

        // Then
        String expected = "Can`t find category by category id " + invalidCategory;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).existsById(invalidCategory);
    }

    @DisplayName("Delete book by valid id")
    @Test
    void deleteBookId_ValidId_ShouldReturnNothing() {
        // Given
        Long id = 1L;
        Book book = createBook();
        book.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // When
        bookService.deleteById(id);

        // Then
        verify(bookRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).deleteById(id);
    }

    @DisplayName("Delete book by invalid id")
    @Test
    void deleteBookId_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 100L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.deleteById(id)
        );

        // Then
        String expected = "Can`t find book by id " + id;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(id);
    }

    @DisplayName("Search book with valid parameters, return list books by parameters")
    @Test
    void search_WithValidSearchParameters_ShouldReturnAllBooksParameters() {
        // Given
        BookSearchParameters params = new BookSearchParameters(
                new String[]{"Book1"}, new String[]{"Author1"});
        Specification<Book> specification = bookSpecificationBuilder.build(params);
        List<Book> bookList = createBookList();

        Book book = createBook();
        book.setId(1L);

        List<BookDto> bookDtoList = createBookDtoList();
        when(bookSpecificationBuilder.build(params)).thenReturn(specification);
        when(bookRepository.findAll(specification)).thenReturn(bookList);
        when(bookMapper.toDto(bookList.get(0))).thenReturn(bookDtoList.get(0));
        when(bookMapper.toDto(bookList.get(1))).thenReturn(bookDtoList.get(1));

        // When
        List<BookDto> result = bookService.search(params);

        // Then
        assertEquals(bookDtoList.get(0), result.get(0));
        assertEquals(bookDtoList.get(1), result.get(1));
        verify(bookSpecificationBuilder, times(2)).build(params);
        verify(bookRepository, times(1)).findAll(specification);
        verify(bookMapper, times(2)).toDto(any(Book.class));

    }

    private List<BookDtoWithoutCategoryIds> withoutCategoryIds() {
        List<BookDtoWithoutCategoryIds> books = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            BookDtoWithoutCategoryIds book = new BookDtoWithoutCategoryIds();
            book.setId(Long.valueOf(i));
            book.setTitle("Book1" + i);
            book.setAuthor("Author1" + i);
            book.setIsbn("12345678" + i);
            book.setPrice(new BigDecimal(10 + i));
            book.setDescription("Description1" + i);
            book.setCoverImage("CoverImage1" + i);
            books.add(book);
        }
        return books;
    }

    private CreateBookRequestDto createBookRequestDto(
    ) {
        return new CreateBookRequestDto()
                .setTitle("Book1")
                .setAuthor("Author1")
                .setIsbn("12345678")
                .setPrice(BigDecimal.valueOf(10))
                .setDescription("Description1")
                .setCoverImage("CoverImage1")
                .setCategoriesIds(List.of(1L));
    }

    private Book createBook() {
        Book book = new Book();
        book.setTitle("Book1");
        book.setAuthor("Author1");
        book.setIsbn("12345678");
        book.setPrice(new BigDecimal(10));
        book.setDescription("Description1");
        book.setCoverImage("CoverImage1");
        book.setCategories(Set.of(category()));
        return book;
    }

    private BookDto createBookDto() {
        return new BookDto()
                .setId(1L)
                .setTitle("Book1")
                .setAuthor("Author1")
                .setIsbn("12345678")
                .setPrice(BigDecimal.valueOf(10))
                .setDescription("Description1")
                .setCoverImage("CoverImage1")
                .setCategoriesIds(List.of(1L));

    }

    private List<BookDto> createBookDtoList() {
        List<BookDto> books = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            BookDto book = new BookDto();
            book.setId(Long.valueOf(i));
            book.setTitle("Book1" + i);
            book.setAuthor("Author1" + i);
            book.setIsbn("12345678" + i);
            book.setPrice(new BigDecimal(10 + i));
            book.setDescription("Description1" + i);
            book.setCoverImage("CoverImage1" + i);
            book.setCategoriesIds(List.of(1L));
            books.add(book);
        }
        return books;
    }

    private Category category() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Category1");
        category.setDescription("Description1");
        return category;
    }

    private List<Book> createBookList() {
        List<Book> books = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            Book book = new Book();
            book.setId(Long.valueOf(i));
            book.setTitle("Book1" + i);
            book.setAuthor("Author1" + i);
            book.setIsbn("12345678" + i);
            book.setPrice(new BigDecimal(10 + i));
            book.setDescription("Description1" + i);
            book.setCoverImage("CoverImage1" + i);
            book.setCategories(Set.of(category()));
            books.add(book);
        }
        return books;
    }
}
