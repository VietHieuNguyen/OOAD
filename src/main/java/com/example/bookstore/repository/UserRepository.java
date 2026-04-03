package com.example.bookstore.repository;

import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Tìm tất cả User theo Role — dùng cho StockNotificationObserver (gửi thông báo cho Admin). */
    List<User> findByRole(Role role);
}
