package com.example.onlinebookstore.service.order.impl;

import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderUpdateStatusDto;
import com.example.onlinebookstore.exception.DataProcessingException;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.OrderMapper;
import com.example.onlinebookstore.model.CartItem;
import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.OrderItem;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.order.OrderRepository;
import com.example.onlinebookstore.repository.orderitem.OrderItemRepository;
import com.example.onlinebookstore.service.order.OrderService;
import com.example.onlinebookstore.service.shoppingcart.ShoppingCartService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShoppingCartService shoppingCartService;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderResponseDto addOrder(User user, OrderRequestDto requestDto) {
        ShoppingCart shoppingCartByUser = shoppingCartService.getShoppingCartByUser(user);

        if (shoppingCartByUser.getCartItems().isEmpty()) {
            throw new DataProcessingException("Can't create order. Shopping cart is empty.");
        }

        BigDecimal total = shoppingCartByUser.getCartItems().stream()
                .map(cartItem -> cartItem.getBook().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);
        order.setTotal(total);
        order.setShippingAddress(requestDto.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        processOrderItems(shoppingCartByUser, savedOrder);
        shoppingCartService.clearShoppingCart(shoppingCartByUser);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public List<OrderResponseDto> findAll(User user, Pageable pageable) {
        List<Order> ordersPage = orderRepository.findByUser(user, pageable);
        return ordersPage.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public OrderResponseDto updateOrderStatusById(Long orderId,
                                                  OrderUpdateStatusDto orderUpdateStatusDto) {
        Order orderSaved = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Can`t find order by id ")
        );
        orderSaved.setStatus(orderUpdateStatusDto.getStatus());
        return orderMapper.toDto(orderRepository.save(orderSaved));
    }

    private void processOrderItems(ShoppingCart shoppingCart, Order order) {
        for (CartItem cartItem : shoppingCart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(cartItem.getBook());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            orderItem.setPrice(cartItem.getBook().getPrice());
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            order.getOrderItems().add(savedOrderItem);
        }
    }
}
