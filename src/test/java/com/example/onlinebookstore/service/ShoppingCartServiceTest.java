package com.example.onlinebookstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.cartitem.CartItemDto;
import com.example.onlinebookstore.dto.cartitem.CartItemResponseDto;
import com.example.onlinebookstore.dto.shoppingcart.ShoppingCartDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.CartItemMapper;
import com.example.onlinebookstore.mapper.ShoppingCartMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.CartItem;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.cartitem.CartItemRepository;
import com.example.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import com.example.onlinebookstore.service.shoppingcart.impl.ShoppingCartServiceImpl;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @DisplayName("Add book shopping cart with invalid id user should return exception")
    @Test
    void addBookToShopCart_ByInvalidUserId_ShouldReturnException() {
        // Given
        User user = createUser();

        when(shoppingCartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> shoppingCartService.findByUser(user)
        );

        // Then
        String expected = "Can`t find by user id";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findByUserId(1L);
    }

    @DisplayName("Add book shopping cart with invalid id book should return exception")
    @Test
    void addBookToShopCart_ByInvalidBookId_ShouldReturnException() {
        // Given
        User user = createUser();
        CartItemDto cartItemDto = createCartItemDto();
        ShoppingCart shoppingCart = createShoppingCart();

        when(shoppingCartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(cartItemDto.getBookId())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> shoppingCartService.addBookToShopCart(user, cartItemDto)
        );

        // Then
        String expected = "Can`t find book id";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(cartItemDto.getBookId());
        verify(shoppingCartRepository, times(1)).findByUserId(1L);
    }

    @DisplayName("Add cart items to shopping cart valid data should return shopping cart"
            + " without cart item mapper")
    @Test
    void addBookToShopCart_ValidTestMethodWithoutCartItemMapper_ShouldReturnSuccess() {
        // Given
        User user = createUser();
        Book book = createBook();
        CartItemDto cartItemDto = createCartItemDto();
        ShoppingCart shoppingCart = createShoppingCart();
        CartItem cartItem = createCartItem();
        shoppingCart.setCartItems(Set.of(cartItem));

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto(1L, 1L,
                Set.of(new CartItemResponseDto(1L, 1L, "Book1", 1)));

        when(shoppingCartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(cartItemDto.getBookId())).thenReturn(Optional.of(book));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartDto);

        // When
        ShoppingCartDto result = shoppingCartService.addBookToShopCart(user, cartItemDto);

        // Then
        assertEquals(shoppingCartDto, result);
        verify(shoppingCartRepository,times(1)).findByUserId(user.getId());
        verify(bookRepository, times(1)).findById(cartItemDto.getBookId());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
    }

    @DisplayName("Add cart items to shopping cart valid data should return shopping cart")
    @Test
    void addBookToShopCart_ValidTestMethod_ShouldReturnSuccess() {
        // Given
        User user = createUser();
        Book book = createBook();
        CartItemDto cartItemDto = createCartItemDto();
        ShoppingCart shoppingCart = createShoppingCart();
        shoppingCart.setCartItems(null);

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto(1L, 1L,
                Set.of(new CartItemResponseDto(1L, 1L, "Book1", 1)));
        CartItem cartItem = createCartItem();

        when(shoppingCartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(cartItemDto.getBookId())).thenReturn(Optional.of(book));
        when(cartItemMapper.toModel(cartItemDto, book)).thenReturn(cartItem);
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartDto);

        // When
        ShoppingCartDto result = shoppingCartService.addBookToShopCart(user, cartItemDto);

        // Then
        assertEquals(shoppingCartDto, result);
        verify(shoppingCartRepository,times(1)).findByUserId(user.getId());
        verify(cartItemMapper, times(1)).toModel(cartItemDto, book);
        verify(bookRepository, times(1)).findById(cartItemDto.getBookId());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
    }

    @DisplayName("Find by user valid id, return shopping cart")
    @Test
    void findByUser_ValidUserId_ReturnShoppingCart() {
        // Given
        ShoppingCart shoppingCart = createShoppingCart();
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto(1L, 1L,
                Set.of(new CartItemResponseDto(1L, 1L, "Book1", 1)));

        when(shoppingCartRepository.findByUserId(
                shoppingCart.getUser().getId())).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartDto);

        // When
        ShoppingCartDto result = shoppingCartService.findByUser(shoppingCart.getUser());

        // Then
        assertEquals(shoppingCartDto, result);
        verify(shoppingCartRepository, times(1))
                .findByUserId(shoppingCart.getUser().getId());
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
    }

    @Test
    void clearShoppingCart_ValidShoppingCartTest_ShouldReturnNothing() {
        // Given
        ShoppingCart shoppingCart = createShoppingCart();
        shoppingCart.setCartItems(new HashSet<>());

        doNothing().when(cartItemRepository).deleteAll(shoppingCart.getCartItems());
        when(shoppingCartRepository.save(shoppingCart)).thenReturn(shoppingCart);

        // When
        shoppingCartService.clearShoppingCart(shoppingCart);

        // Then
        verify(cartItemRepository, times(1)).deleteAll(shoppingCart.getCartItems());
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
    }

    private CartItem createCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(createShoppingCart());
        cartItem.setBook(createBook());
        cartItem.setQuantity(1);
        return cartItem;
    }

    private Book createBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Book1");
        book.setAuthor("Author1");
        book.setIsbn("12345678");
        book.setPrice(new BigDecimal(10));
        book.setDescription("Description1");
        book.setCoverImage("CoverImage1");
        book.setCategories(Set.of(createCategory()));
        return book;
    }

    private Category createCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Category1");
        category.setDescription("Description1");
        return category;
    }

    private ShoppingCart createShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setUser(createUser());
        shoppingCart.setCartItems(new HashSet<>());
        return shoppingCart;
    }

    private CartItemDto createCartItemDto() {
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setBookId(1L);
        cartItemDto.setQuantity(1);
        return cartItemDto;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bob");
        user.setLastName("Bobson");
        user.setPassword("12345678");
        user.setEmail("bob@example.com");
        user.setShippingAddress("Shevchenka");
        return user;
    }
}
