package com.example.bookstore.repository;

import com.example.bookstore.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, String> {

    Optional<Cart> findByCustomer_Id(String customerId);
}
