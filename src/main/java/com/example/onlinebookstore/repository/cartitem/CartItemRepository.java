package com.example.onlinebookstore.repository.cartitem;

import com.example.onlinebookstore.model.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @EntityGraph(attributePaths = "shoppingCart")
    Optional<CartItem> findById(Long id);
}
