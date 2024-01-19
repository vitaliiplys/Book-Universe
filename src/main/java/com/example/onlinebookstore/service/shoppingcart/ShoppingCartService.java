package com.example.onlinebookstore.service.shoppingcart;

import com.example.onlinebookstore.dto.cartitem.CartItemDto;
import com.example.onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;

public interface ShoppingCartService {
    ShoppingCartDto addBookShopCart(User user, CartItemDto cartItemDto);

    ShoppingCartDto findByUser(User user);

    ShoppingCart getShoppingCartByUser(User user);

    void clearShoppingCart(ShoppingCart shoppingCart);
}
