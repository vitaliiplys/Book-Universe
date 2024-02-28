package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.category.CategoryDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.CategoryMapper;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.repository.category.CategoryRepository;
import com.example.onlinebookstore.service.category.impl.CategoryServiceImpl;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("Create a new category")
    void save_Valid_CreateCategoryRequestDto_ReturnsCategoryDto() {
        // Given
        CategoryDto createCategoryDto = createCategoryDto();

        Category category = new Category();
        category.setId(createCategoryDto.id());
        category.setName(createCategoryDto.name());
        category.setDescription(createCategoryDto.description());

        CategoryDto expected = expectedCategoryDto(createCategoryDto);

        when(categoryMapper.toEntity(createCategoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        // When
        CategoryDto result = categoryService.save(createCategoryDto);

        // Then
        Assertions.assertEquals(createCategoryDto, result);
        assertThat(result).isEqualTo(createCategoryDto);
        verify(categoryMapper, times(1)).toEntity(createCategoryDto);
        verify(categoryRepository,times(1)).save(category);
        verify(categoryMapper,times(1)).toDto(category);
    }

    @Test
    @DisplayName("Find all category valid")
    void findAll_ValidPageable_ReturnAllCategory() {
        // Given
        Category category = createCategory();

        CategoryDto categoryDto = new CategoryDto(
                category.getId(), category.getName(),
                category.getDescription());

        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categoryList = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categoryList, pageable, categoryList.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        List<CategoryDto> categoryDtos = categoryService.findAll(pageable);

        // Then
        assertThat(categoryDtos).hasSize(1);
        assertThat(categoryDtos.get(0)).isEqualTo(categoryDto);
        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Get category by valid id")
    void getCategory_ByValidCategoryId_ShouldReturnCategory() {
        // Given
        Long categoryId = 1L;
        Category category = createCategory();

        CategoryDto expectedCategory = createCategoryDto();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(expectedCategory);

        // When
        CategoryDto result = categoryService.findById(categoryId);

        // Then
        Assertions.assertEquals(expectedCategory, result);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1)).toDto(category);

    }

    @Test
    @DisplayName("Get category by invalid id")
    void getCategory_ByInvalidId_ShouldReturnException() {
        // Given
        Long categoryId = 100L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.findById(categoryId)
        );

        // Then
        String expected = "Can't find category with id " + categoryId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Update category by valid id")
    void updateCategoryById_ValidId_ShouldReturnCategoryUpdate() {
        // Given
        Category updateCategory = new Category();
        updateCategory.setId(1L);
        updateCategory.setName("Update Category");
        updateCategory.setDescription("Update Description");

        CategoryDto categoryDto = createCategoryDto();
        Long categoryId = 1L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(updateCategory));
        when(categoryMapper.toEntity(categoryDto)).thenReturn(updateCategory);
        when(categoryRepository.save(updateCategory)).thenReturn(updateCategory);
        when(categoryMapper.toDto(updateCategory)).thenReturn(categoryDto);

        // When
        CategoryDto result = categoryService.update(categoryId, categoryDto);

        // Then
        Assertions.assertEquals(categoryDto, result);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1)).toDto(updateCategory);
        verify(categoryRepository, times(1)).save(updateCategory);
        verify(categoryMapper, times(1)).toEntity(categoryDto);
    }

    @Test
    @DisplayName("Update category by invalid id")
    void updateCategory_ByInvalidId_ReturnShouldException() {
        // Given
        Long id = 100L;
        CategoryDto categoryDto = new CategoryDto(id, "Update Category", "Update Description");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.update(id, categoryDto)
        );

        // Then
        String expected = "Category with id " + id + " was not found";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Delete category by valid id")
    void deleteCategoryId_ValidId_ShouldReturnNothing() {
        // Given
        Long id = 1L;
        Category category = createCategory();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        // When
        categoryService.deleteById(id);

        // Then
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Delete category by invalid id")
    void deleteCategory_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 1L;

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.deleteById(id)
        );

        // Then
        String expected = "Can`t find category by id " + id;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(id);
    }

    private Category createCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Category1");
        category.setDescription("Description1");
        return category;
    }

    private CategoryDto createCategoryDto() {
        return new CategoryDto(1L, "Category1", "Description1");
    }

    private CategoryDto expectedCategoryDto(CategoryDto responseCategoryDto) {
        return new CategoryDto(
                responseCategoryDto.id(),
                responseCategoryDto.name(),
                responseCategoryDto.description());
    }
}
