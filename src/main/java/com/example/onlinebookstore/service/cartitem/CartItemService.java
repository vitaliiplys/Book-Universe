package com.example.onlinebookstore.service.cartitem;

import com.example.onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import com.example.onlinebookstore.model.User;

public interface CartItemService {
    ShoppingCartDto updateBookQuantityById(Long id, int quantity);

    void deleteItemById(User user, Long id);
}
