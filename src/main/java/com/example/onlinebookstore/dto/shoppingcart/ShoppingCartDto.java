package com.example.onlinebookstore.dto.shoppingcart;

import com.example.onlinebookstore.dto.cartitem.CartItemResponseDto;
import java.util.Set;

public record ShoppingCartDto(
        Long id,
        Long userId,
        Set<CartItemResponseDto> cartItems
) {
}
