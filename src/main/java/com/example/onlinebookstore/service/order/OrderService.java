package com.example.onlinebookstore.service.order;

import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderUpdateStatusDto;
import com.example.onlinebookstore.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto addOrder(User principal, OrderRequestDto requestDto);

    List<OrderResponseDto> findAll(User user, Pageable pageable);

    OrderResponseDto updateOrderStatusById(Long orderId,
                                           OrderUpdateStatusDto orderUpdateStatusDto);
}
