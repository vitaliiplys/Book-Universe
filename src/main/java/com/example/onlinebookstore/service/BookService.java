package com.example.onlinebookstore.service;

import com.example.onlinebookstore.dto.BookDto;
import com.example.onlinebookstore.dto.CreateBookRequestDto;
import java.util.List;

public interface BookService {

    List<BookDto> findAll();

    BookDto findBookById(Long id);

    BookDto save(CreateBookRequestDto bookDto);
}
