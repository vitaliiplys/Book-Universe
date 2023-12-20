package com.example.onlinebookstore.repository.shoppingcart;

import com.example.onlinebookstore.model.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @EntityGraph(attributePaths = "cartItems")
    Optional<ShoppingCart> findByUserId(Long id);
}
