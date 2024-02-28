package com.example.onlinebookstore.dto.category;

import jakarta.validation.constraints.NotNull;

public record CategoryDto(
        Long id,
        @NotNull
        String name,
        String description
) {
}
