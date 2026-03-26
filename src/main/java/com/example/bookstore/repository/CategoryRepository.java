package com.example.bookstore.repository;

import com.example.bookstore.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByName(String name);
}
