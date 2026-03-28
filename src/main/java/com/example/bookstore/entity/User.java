package com.example.bookstore.entity;

import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.entity.enums.Role;
import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(unique = true, length = 150)
    private String email;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(length = 500)
    private String token;

    @Column(length = 500)
    private String avatar;

    @PrePersist
    protected void prePersist() {
        if (id == null || id.isBlank()) {
            id = IdGenerator.newId();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (authProvider == null) {
            authProvider = AuthProvider.LOCAL;
        }
    }
}
