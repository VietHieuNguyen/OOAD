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
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "category_id", length = 36, nullable = false, updatable = false)
    private String categoryId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ===== Parent-Child Hierarchy =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private Set<Category> children = new LinkedHashSet<>();

    // ===================================

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Book> books = new LinkedHashSet<>();

    @PrePersist
    protected void prePersist() {
        if (categoryId == null || categoryId.isBlank()) {
            categoryId = IdGenerator.newId();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * Trả về tên đầy đủ bao gồm parent: "Parent > Child"
     */
    public String getFullName() {
        if (parentCategory != null) {
            return parentCategory.getName() + " > " + name;
        }
        return name;
    }
}
