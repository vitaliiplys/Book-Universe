package com.example.onlinebookstore.service.orderitem.impl;

import com.example.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.OrderItemMapper;
import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.OrderItem;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.order.OrderRepository;
import com.example.onlinebookstore.service.orderitem.OrderItemService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemMapper orderItemMapper;
    private final OrderRepository orderRepository;

    @Override
    public List<OrderItemResponseDto> findByIdAndUser(Long orderId, User user) {
        Order order = orderRepository.findByIdAndUser(orderId, user).orElseThrow(
                () -> new EntityNotFoundException("Order not found for id " + orderId)
        );
        Set<OrderItem> orderItems = order.getOrderItems();
        return orderItems.stream()
                .map(orderItemMapper::toDto)
                .toList();
    }

    @Override
    public OrderItemResponseDto findByUserAndIdAndOrderItems_Id(User user,
                                                                Long orderId,
                                                                Long itemId) {
        Order orderSaved = orderRepository.findByUserAndIdAndOrderItems_Id(
                user, orderId, itemId).orElseThrow(
                        () -> new EntityNotFoundException(
                        "Can`t find order and order items the user id " + itemId)
                );
        Set<OrderItem> orderItems = orderSaved.getOrderItems();
        OrderItem orderItemSaved = orderItems.stream()
                .filter(orderItem -> orderItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Can`t find order item for id " + itemId));
        return orderItemMapper.toDto(orderItemSaved);
    }
}
