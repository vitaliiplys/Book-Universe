package com.example.onlinebookstore.dto.cartitem;

public record CartItemResponseDto(
        Long id,
        Long bookId,
        String bookTitle,
        int quantity
) {
}
