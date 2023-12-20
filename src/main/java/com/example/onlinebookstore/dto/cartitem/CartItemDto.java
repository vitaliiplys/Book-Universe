package com.example.onlinebookstore.dto.cartitem;

import lombok.Data;

@Data
public class CartItemDto {
    private Long bookId;
    private int quantity;
}
