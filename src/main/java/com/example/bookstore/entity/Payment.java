package com.example.bookstore.entity;

import com.example.bookstore.entity.enums.PaymentMethodType;
import com.example.bookstore.entity.enums.PaymentStatus;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "payment_id", length = 36, nullable = false, updatable = false)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethodType method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @PrePersist
    protected void prePersist() {
        if (paymentId == null || paymentId.isBlank()) {
            paymentId = IdGenerator.newId();
        }
        if (status == null) {
            status = PaymentStatus.UNPAID;
        }
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }
}
