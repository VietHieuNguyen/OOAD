package com.example.bookstore.entity;

import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity đại diện cho Mã giảm giá (Voucher/Coupon).
 *
 * <p>Được sử dụng bởi {@code VoucherDiscountDecorator} trong Decorator Pattern
 * để tính toán giảm giá linh hoạt cho giỏ hàng.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @Column(name = "voucher_id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_percentage", nullable = false)
    private Double discountPercentage;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 100;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void prePersist() {
        if (id == null || id.isBlank()) {
            id = IdGenerator.newId();
        }
        if (usageLimit == null) usageLimit = 100;
        if (usedCount == null) usedCount = 0;
        if (isActive == null) isActive = true;
    }

    /**
     * Kiểm tra xem voucher còn hợp lệ để sử dụng hay không.
     */
    public boolean isValid() {
        if (!isActive) return false;
        if (usedCount >= usageLimit) return false;
        if (expirationDate != null && LocalDateTime.now().isAfter(expirationDate)) return false;
        return true;
    }
}
