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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @Column(name = "order_item_id", length = 36, nullable = false, updatable = false)
    private String orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(name = "is_gift_wrapped", nullable = false)
    private Boolean isGiftWrapped = false;

    @PrePersist
    protected void prePersist() {
        if (orderItemId == null || orderItemId.isBlank()) {
            orderItemId = IdGenerator.newId();
        }
    }

    /**
     * Tính thành tiền của một dòng sản phẩm trong đơn hàng.
     * <p>Công thức: {@code quantity × priceAtPurchase}</p>
     *
     * @return Thành tiền (subtotal) của OrderItem này
     */
    public BigDecimal getSubTotal() {
        if (priceAtPurchase == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}

