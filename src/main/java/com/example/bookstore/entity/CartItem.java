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
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @Column(name = "cart_item_id", length = 36, nullable = false, updatable = false)
    private String cartItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "is_selected", nullable = false)
    private Boolean isSelected = true;

    @Column(name = "is_gift_wrapped", nullable = false)
    private Boolean isGiftWrapped = false;

    @PrePersist
    protected void prePersist() {
        if (cartItemId == null || cartItemId.isBlank()) {
            cartItemId = IdGenerator.newId();
        }
    }
}
