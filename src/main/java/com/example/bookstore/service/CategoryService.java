package com.example.bookstore.service;

import com.example.bookstore.entity.Category;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer cho entity Category.
 * <p>Tach rieng khoi BookService de dam bao nguyen tac Single Responsibility.
 * Cung cap cac nghiep vu CRUD cho chuc nang Admin quan ly danh muc.</p>
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Lay tat ca danh muc, sap xep theo ten.
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Tim danh muc theo ID.
     */
    @Transactional(readOnly = true)
    public Optional<Category> findById(String id) {
        return categoryRepository.findById(id);
    }

    /**
     * Luu (them moi hoac cap nhat) mot danh muc.
     *
     * @param category doi tuong Category da duoc bind du lieu tu form/modal
     * @return Category da duoc luu (co ID)
     */
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Xoa danh muc theo ID.
     * <p><b>Rang buoc du lieu:</b> Chi cho phep xoa danh muc khi khong con
     * sach nao tham chieu den no (tranh loi Foreign Key Constraint).</p>
     *
     * @param id ID cua danh muc can xoa
     * @throws IllegalStateException neu danh muc van con sach ben trong
     * @throws IllegalArgumentException neu khong tim thay danh muc
     */
    @Transactional
    public void deleteById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khong tim thay danh muc voi ID: " + id));

        // Kiem tra rang buoc: khong cho xoa neu con sach trong danh muc
        long bookCount = countBooksInCategory(id);
        if (bookCount > 0) {
            throw new IllegalStateException(
                    "Khong the xoa danh muc \"" + category.getName()
                    + "\" vi hien dang co " + bookCount + " cuon sach thuoc danh muc nay. "
                    + "Hay chuyen cac cuon sach sang danh muc khac truoc khi xoa.");
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Dem so luong sach thuoc mot danh muc.
     * Dung de hien thi so lieu tren giao dien va kiem tra rang buoc khi xoa.
     */
    @Transactional(readOnly = true)
    public long countBooksInCategory(String categoryId) {
        return bookRepository.findByCategoryCategoryId(categoryId).size();
    }

    /**
     * Kiem tra ten danh muc da ton tai chua.
     */
    @Transactional(readOnly = true)
    public boolean nameExists(String name) {
        return categoryRepository.findByName(name).isPresent();
    }

    /**
     * Lay danh sach sach thuoc mot danh muc cu the.
     * Force-init lazy category de tranh LazyInitializationException trong Thymeleaf.
     */
    @Transactional(readOnly = true)
    public java.util.List<com.example.bookstore.entity.Book> findBooksInCategory(String categoryId) {
        java.util.List<com.example.bookstore.entity.Book> books = bookRepository.findByCategoryCategoryId(categoryId);
        books.forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return books;
    }

    /**
     * Lấy danh sách danh mục gốc (không có parent).
     */
    @Transactional(readOnly = true)
    public List<Category> findRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    /**
     * Lấy danh sách danh mục con của một danh mục cha.
     */
    @Transactional(readOnly = true)
    public List<Category> findChildCategories(String parentId) {
        return categoryRepository.findByParentCategoryCategoryId(parentId);
    }
}
