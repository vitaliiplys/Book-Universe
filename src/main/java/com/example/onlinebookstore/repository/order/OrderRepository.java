package com.example.onlinebookstore.repository.order;

import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "user")
    List<Order> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByIdAndUser(Long orderId, User user);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByUserAndIdAndOrderItems_Id(User user, Long orderId, Long itemId);
}
