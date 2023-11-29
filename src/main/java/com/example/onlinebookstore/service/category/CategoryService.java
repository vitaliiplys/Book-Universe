package com.example.onlinebookstore.service.category;

import com.example.onlinebookstore.dto.category.CategoryDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    List<CategoryDto> findAll(Pageable pageable);

    CategoryDto findById(Long id);

    CategoryDto save(CategoryDto categoryResponseDto);

    CategoryDto update(Long id, CategoryDto categoryResponseDto);

    void deleteById(Long id);
}
