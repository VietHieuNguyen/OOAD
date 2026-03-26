package com.example.bookstore.repository;

import com.example.bookstore.entity.CartItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByCart_CartId(String cartId);
}
