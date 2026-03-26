package com.example.bookstore.entity;

import com.example.bookstore.entity.enums.ShippingStatus;
import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "shippings")
public class Shipping {

    @Id
    @Column(name = "shipping_id", length = 36, nullable = false, updatable = false)
    private String shippingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "provider_name", length = 150)
    private String providerName;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingStatus status;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @PrePersist
    protected void prePersist() {
        if (shippingId == null || shippingId.isBlank()) {
            shippingId = IdGenerator.newId();
        }
        if (status == null) {
            status = ShippingStatus.PREPARING;
        }
    }
}
