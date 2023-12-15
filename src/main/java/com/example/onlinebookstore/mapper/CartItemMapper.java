package com.example.onlinebookstore.mapper;

import com.example.onlinebookstore.config.MapperConfig;
import com.example.onlinebookstore.dto.cartitem.CartItemDto;
import com.example.onlinebookstore.dto.cartitem.CartItemResponseDto;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CartItemMapper {

    @Mapping(target = "bookId", source = "cartItem.book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    CartItemResponseDto toDto(CartItem cartItem);

    @Mapping(target = "book", source = "book")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoppingCart", ignore = true)
    CartItem toModel(CartItemDto cartItemDto, Book book);
}
