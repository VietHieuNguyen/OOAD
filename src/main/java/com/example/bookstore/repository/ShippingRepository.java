package com.example.bookstore.repository;

import com.example.bookstore.entity.Shipping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRepository extends JpaRepository<Shipping, String> {

    List<Shipping> findByOrder_OrderId(String orderId);
}
