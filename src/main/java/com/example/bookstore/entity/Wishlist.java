package com.example.bookstore.entity;

import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity lưu sản phẩm yêu thích (Wishlist) của khách hàng.
 * Mỗi khách hàng chỉ có thể thêm cùng 1 quyển sách vào wishlist 1 lần.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wishlists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_id", "book_id"})
})
public class Wishlist {

    @Id
    @Column(name = "wishlist_id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void prePersist() {
        if (id == null || id.isBlank()) {
            id = IdGenerator.newId();
        }
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
}
