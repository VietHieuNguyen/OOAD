package com.example.bookstore.repository;

import com.example.bookstore.entity.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {

    Optional<Admin> findByUsername(String username);
}
