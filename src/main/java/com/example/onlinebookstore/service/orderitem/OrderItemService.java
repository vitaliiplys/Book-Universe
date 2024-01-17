package com.example.onlinebookstore.service.orderitem;

import com.example.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import com.example.onlinebookstore.model.User;
import java.util.List;

public interface OrderItemService {

    List<OrderItemResponseDto> findByIdAndUser(Long orderId, User user);

    OrderItemResponseDto findByUserAndIdAndOrderItems_Id(User user, Long orderId, Long itemId);
}
