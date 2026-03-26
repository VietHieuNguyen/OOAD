package com.example.bookstore.repository;

import com.example.bookstore.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByOrder_OrderId(String orderId);
}
