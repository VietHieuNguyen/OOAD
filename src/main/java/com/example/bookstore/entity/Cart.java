package com.example.bookstore.entity;

import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @Column(name = "cart_id", length = 36, nullable = false, updatable = false)
    private String cartId;

    @OneToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new LinkedHashSet<>();

    @PrePersist
    protected void prePersist() {
        if (cartId == null || cartId.isBlank()) {
            cartId = IdGenerator.newId();
        }
    }
}
