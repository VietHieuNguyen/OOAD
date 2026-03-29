package com.example.bookstore.repository;

import com.example.bookstore.entity.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, String> {

    List<Wishlist> findByCustomer_IdOrderByAddedAtDesc(String customerId);

    Optional<Wishlist> findByCustomer_IdAndBook_Id(String customerId, String bookId);

    boolean existsByCustomer_IdAndBook_Id(String customerId, String bookId);
}
