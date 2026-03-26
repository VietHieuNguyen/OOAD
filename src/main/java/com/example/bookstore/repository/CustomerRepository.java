package com.example.bookstore.repository;

import com.example.bookstore.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByUsername(String username);
}
