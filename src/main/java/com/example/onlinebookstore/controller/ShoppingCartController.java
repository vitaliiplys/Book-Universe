package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.dto.cartitem.CartItemDto;
import com.example.onlinebookstore.dto.cartitem.CartItemUpdateRequestDto;
import com.example.onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.service.cartitem.CartItemService;
import com.example.onlinebookstore.service.shoppingcart.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart management", description = "Endpoints for managing shopping carts")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    private final CartItemService cartItemService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Add book", description = "Add book to shopping cart")
    public ShoppingCartDto addBookShopCart(@RequestBody CartItemDto cartItemDto,
                                           Authentication authentication) {
        return shoppingCartService
                .addBookShopCart((User) authentication.getPrincipal(), cartItemDto);
    }

    @GetMapping
    @Operation(summary = "Get shopping cart", description = "Get users shopping cart")
    public ShoppingCartDto getUserShoppingCart(Authentication authentication) {
        return shoppingCartService.findByUser((User) authentication.getPrincipal());
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/cart-items/{id}")
    @Operation(summary = "Update quantity", description = "Update quantity from cart item")
    public ShoppingCartDto updateBookQuantityById(@PathVariable Long id,
                                                  @RequestBody CartItemUpdateRequestDto request) {
        return cartItemService.updateBookQuantityById(id, request.quantity());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/cart-items/{id}")
    @Operation(summary = "Delete cart item", description = "Delete cart item from shopping cart")
    public void deleteItemById(Authentication authentication, @PathVariable Long id) {
        cartItemService.deleteItemById((User) authentication.getPrincipal(), id);
    }
}
