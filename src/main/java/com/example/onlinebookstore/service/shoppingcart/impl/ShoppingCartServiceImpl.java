package com.example.onlinebookstore.service.shoppingcart.impl;

import com.example.onlinebookstore.dto.cartitem.CartItemDto;
import com.example.onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.CartItemMapper;
import com.example.onlinebookstore.mapper.ShoppingCartMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.CartItem;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.cartitem.CartItemRepository;
import com.example.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import com.example.onlinebookstore.service.shoppingcart.ShoppingCartService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    public ShoppingCartDto addBookToShopCart(User user, CartItemDto cartItemDto) {
        ShoppingCart shoppingCart = getShoppingCartByUser(user);
        Set<CartItem> cartItems = shoppingCart.getCartItems();
        if (cartItems == null) {
            cartItems = new HashSet<>();
        }

        CartItem itemToSave = null;

        boolean bookexist = false;
        for (CartItem cartItem : cartItems) {
            if (cartItemDto != null && cartItem.getBook().getId().equals(cartItemDto.getBookId())) {
                cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
                itemToSave = cartItem;
                bookexist = true;
            }
        }
        Book book = bookRepository.findById(cartItemDto.getBookId()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find book id")
        );
        if (!bookexist) {
            itemToSave = cartItemMapper.toModel(cartItemDto, book);
            itemToSave.setShoppingCart(shoppingCart);
            cartItems.add(itemToSave);
        }
        cartItemRepository.save(itemToSave);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto findByUser(User user) {
        ShoppingCart shoppingCart = getShoppingCartByUser(user);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCart getShoppingCartByUser(User user) {
        return shoppingCartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can`t find by user id"));
    }

    @Override
    public void clearShoppingCart(ShoppingCart shoppingCart) {
        cartItemRepository.deleteAll(shoppingCart.getCartItems());
        shoppingCart.setCartItems(new HashSet<>());
        shoppingCartRepository.save(shoppingCart);
    }
}
