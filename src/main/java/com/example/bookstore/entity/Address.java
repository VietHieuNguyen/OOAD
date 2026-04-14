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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lưu nhiều địa chỉ giao hàng cho một Customer.
 * Mỗi customer có thể có nhiều địa chỉ, 1 trong số đó được đánh dấu isDefault = true.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @Column(name = "address_id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Tên người nhận (có thể khác tên account). */
    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    /** Số điện thoại người nhận. */
    @Column(name = "phone", length = 20)
    private String phone;

    /** Địa chỉ đầy đủ: số nhà, đường, phường/xã. */
    @Column(nullable = false, length = 500)
    private String address;

    /** Thành phố / tỉnh. */
    @Column(name = "city", length = 100)
    private String city;

    /** Nhãn: "Home", "Work", "Other". */
    @Column(name = "label", length = 50)
    private String label;

    /** Đánh dấu địa chỉ mặc định. Chỉ 1 địa chỉ được default = true trên mỗi customer. */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @PrePersist
    protected void prePersist() {
        if (id == null || id.isBlank()) {
            id = IdGenerator.newId();
        }
        if (isDefault == null) isDefault = false;
    }
}
