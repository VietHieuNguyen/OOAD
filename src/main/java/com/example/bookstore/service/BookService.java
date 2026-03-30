package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer cho entity Book.
 * <p>Cung cap cac nghiep vu CRUD phuc vu cho ca Client (hien thi) va Admin (quan ly).
 * Duoc thiet ke de thanh vien khac co the dap Observer Pattern vao method
 * {@link #save(Book)} hoac {@link #updateStock(String, int)} ma khong can chinh sua nhieu.</p>
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    // ======================== READ ========================

    /**
     * Lay tat ca sach, force-init Lazy category de tranh LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return books;
    }

    /**
     * Lay sach co phan trang.
     */
    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        Page<Book> page = bookRepository.findAll(pageable);
        page.getContent().forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return page;
    }

    /**
     * Tim sach theo ID. Tra ve Optional de Controller xu ly truong hop khong tim thay.
     */
    @Transactional(readOnly = true)
    public Optional<Book> findById(String id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        bookOpt.ifPresent(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return bookOpt;
    }

    /**
     * Tim sach theo tu khoa (ten sach). Phuc vu chuc nang Search cua Admin.
     */
    @Transactional(readOnly = true)
    public List<Book> searchByTitle(String keyword) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(keyword);
        books.forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return books;
    }

    /**
     * Lay tat ca danh muc de hien thi trong dropdown <select> cua form.
     */
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    // ======================== CREATE / UPDATE ========================

    /**
     * Luu (them moi hoac cap nhat) mot quyen sach vao database.
     * <p><b>Observer Pattern Hook:</b> Sau khi save thanh cong, thanh vien phu trach
     * Observer co the chen event "StockChanged" o day.</p>
     *
     * @param book doi tuong Book da duoc bind du lieu tu form
     * @return Book da duoc luu (co ID)
     */
    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Cap nhat so luong ton kho cho mot quyen sach cu the.
     * <p><b>Observer Pattern Hook:</b> Day la method ly tuong de trigger
     * thong bao "Het hang" hoac "Sap het hang" qua Observer.</p>
     *
     * @param bookId  ID cua sach can cap nhat
     * @param newStock so luong ton kho moi
     * @throws IllegalArgumentException neu khong tim thay sach
     */
    @Transactional
    public void updateStock(String bookId, int newStock) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay sach voi ID: " + bookId));
        book.setStockQuantity(newStock);
        bookRepository.save(book);
        // TODO [Observer]: Goi notifyObservers(book) tai day khi ket noi Observer Pattern
    }

    // ======================== DELETE ========================

    /**
     * Xoa mot quyen sach theo ID.
     *
     * @param id ID cua sach can xoa
     * @throws IllegalArgumentException neu khong tim thay sach
     */
    @Transactional
    public void deleteById(String id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Khong tim thay sach voi ID: " + id);
        }
        bookRepository.deleteById(id);
    }

    /**
     * Kiem tra ISBN da ton tai trong database chua (dung de validate form tao moi).
     */
    @Transactional(readOnly = true)
    public boolean isbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }
}
