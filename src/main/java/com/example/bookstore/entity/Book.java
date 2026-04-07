package com.example.bookstore.entity;

import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @Column(name = "book_id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String thumbnail;

    /** Chuỗi định danh không dấu, dùng cho URL thân thiện (Unique). */
    @Column(unique = true, length = 300)
    private String slug;

    /** Đánh dấu sách được chọn hiển thị trong mục "Staff Pick". */
    @Column(name = "is_picked", nullable = false)
    private Boolean isPicked = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @PrePersist
    protected void prePersist() {
        if (id == null || id.isBlank()) {
            id = IdGenerator.newId();
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (isPicked == null) {
            isPicked = false;
        }
        if ((slug == null || slug.isBlank()) && title != null) {
            slug = generateSlug(title);
        }
    }

    /**
     * Tạo slug từ title: loại bỏ dấu tiếng Việt, chuyển thành lowercase,
     * thay khoảng trắng bằng dấu gạch ngang, loại bỏ ký tự đặc biệt.
     */
    private String generateSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Loại bỏ dấu kết hợp (combining marks) — xử lý tiếng Việt
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noDiacritics = pattern.matcher(normalized).replaceAll("");
        // Xử lý ký tự đặc biệt tiếng Việt: đ → d, Đ → D
        noDiacritics = noDiacritics.replace('đ', 'd').replace('Đ', 'D');
        return noDiacritics.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
