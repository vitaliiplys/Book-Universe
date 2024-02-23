package com.example.onlinebookstore.service.book.impl;

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
import com.example.onlinebookstore.service.book.BookService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;
    private final CategoryRepository categoryRepository;

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find book by id " + id)
        );
        return bookMapper.toDto(book);
    }

    @Override
    public BookDto save(CreateBookRequestDto bookDto) {
        Book book = bookMapper.toModel(bookDto);
        book.setCategories(getCategoriesByIds(bookDto.getCategoriesIds()));
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        if (bookRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Can`t find book by id " + id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public BookDto updateById(Long id, CreateBookRequestDto updateDto) {
        if (bookRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Can`t find book by id " + id);
        }
        Set<Category> categoriesByIds = getCategoriesByIds(updateDto.getCategoriesIds());
        Book book = bookMapper.toModel(updateDto);
        book.setId(id);
        book.setCategories(categoriesByIds);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> search(BookSearchParameters params) {
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(params);
        return bookRepository.findAll(bookSpecification)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDtoWithoutCategoryIds> findAllByCategoryId(Long categoryId) {
        if (!bookRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Can`t find category by category id " + categoryId);
        }
        return bookRepository.findAllByCategoriesId(categoryId).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }

    private Set<Category> getCategoriesByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            throw new EntityNotFoundException("Cant find categories by Empty ids list");
        }
        return ids.stream()
                .map(categoryRepository::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }
}
