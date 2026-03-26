package com.example.bookstore.repository;

import com.example.bookstore.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCustomer_Id(String customerId);
}
