package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository cho entity Book.
 */
public interface BookRepository extends JpaRepository<Book, String> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String keyword);

    List<Book> findByCategoryCategoryId(String categoryId);

    Optional<Book> findBySlug(String slug);

    List<Book> findByIsPickedTrue();

    /** Sách active, không bị xoá và còn hàng — dùng cho hiển thị client (home, collections). */
    List<Book> findByIsActiveTrueAndIsDeletedFalseAndStockQuantityGreaterThan(int minStock);

    /** Tìm sách liên quan phân trang. */
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.isDeleted = false AND b.stockQuantity > 0 AND b.category.categoryId = :categoryId AND b.bookId <> :bookId")
    List<Book> findRelatedBooks(@Param("categoryId") String categoryId, @Param("bookId") String bookId, Pageable pageable);

    /** Paginated category filter. */
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.isDeleted = false AND b.stockQuantity > :minStock AND b.category.categoryId = :categoryId")
    Page<Book> findByCategoryCategoryIdAndIsActiveTrueAndIsDeletedFalseAndStockQuantityGreaterThan(@Param("categoryId") String categoryId, @Param("minStock") int minStock, Pageable pageable);

    /** Paginated all active. */
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.isDeleted = false AND b.stockQuantity > :minStock")
    Page<Book> findByIsActiveTrueAndIsDeletedFalseAndStockQuantityGreaterThan(@Param("minStock") int minStock, Pageable pageable);

    /** Paginated search by title OR slug. */
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.isDeleted = false AND b.stockQuantity > 0 AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.slug) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Book> searchByTitleOrSlug(@Param("q") String q, Pageable pageable);

    /** Paginated: category + search. */
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND b.isDeleted = false AND b.stockQuantity > 0 AND b.category.categoryId = :catId AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.slug)  LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Book> searchByCategoryAndQuery(@Param("catId") String catId,
                                        @Param("q") String q,
                                        Pageable pageable);

    /** Paginated: filter by max price. */
    @Query("SELECT b FROM Book b WHERE b.price <= :maxPrice")
    Page<Book> findByMaxPrice(@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
}
