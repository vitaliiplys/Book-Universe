package com.example.onlinebookstore.repository.orderitem;

import com.example.onlinebookstore.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
