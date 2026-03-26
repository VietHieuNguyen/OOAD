package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {

    Optional<Book> findByIsbn(String isbn);
}
