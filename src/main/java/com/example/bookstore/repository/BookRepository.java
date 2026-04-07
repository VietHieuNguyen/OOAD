package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho entity Book.
 * Cung cap cac phuong thuc truy van co ban va mo rong cho chuc nang Admin CRUD.
 */
public interface BookRepository extends JpaRepository<Book, String> {

    Optional<Book> findByIsbn(String isbn);

    /** Kiem tra ISBN da ton tai chua (dung khi tao sach moi). */
    boolean existsByIsbn(String isbn);

    /** Tim sach theo ten (khong phan biet hoa thuong), phuc vu chuc nang Search. */
    List<Book> findByTitleContainingIgnoreCase(String keyword);

    /** Loc sach theo danh muc. */
    List<Book> findByCategoryId(String categoryId);

    /** Tìm sách theo slug (URL thân thiện). */
    Optional<Book> findBySlug(String slug);

    /** Lấy danh sách sách được đánh dấu Staff Pick. */
    List<Book> findByIsPickedTrue();
}
