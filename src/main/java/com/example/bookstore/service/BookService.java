package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.pattern.observer.StockObserver;
import com.example.bookstore.pattern.observer.StockSubject;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer cho entity Book.
 *
 * <p>Cung cap cac nghiep vu CRUD phuc vu cho ca Client (hien thi) va Admin (quan ly).</p>
 *
 * <p><b>Observer Pattern (StockSubject):</b> Class nay implements {@link StockSubject}.
 * Spring Boot tu dong inject toan bo bean {@code @Component} co implements {@link StockObserver}
 * vao {@code List<StockObserver>} thong qua Constructor Injection.
 * Nguyen tac OCP duoc tuan thu: them Observer moi chi can tao file moi voi {@code @Component},
 * khong can sua file nay.</p>
 */
@Service
public class BookService implements StockSubject {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Spring tu dong gom tat ca @Component implements StockObserver vao list nay.
     * Khong can khai bao tung Observer cu the - tuan thu Open/Closed Principle.
     */
    private final List<StockObserver> stockObservers;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       List<StockObserver> stockObservers) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        // Wrap bằng ArrayList để đảm bảo list có thể modify (add/remove) được.
        // Spring inject List<?> đôi khi là unmodifiableList — cần wrap để an toàn.
        this.stockObservers = new ArrayList<>(stockObservers);
    }

    // ======================== OBSERVER PATTERN ========================

    @Override
    public void addStockObserver(StockObserver observer) {
        stockObservers.add(observer);
    }

    @Override
    public void removeStockObserver(StockObserver observer) {
        stockObservers.remove(observer);
    }

    @Override
    public void notifyStockObservers(Book book) {
        stockObservers.forEach(observer -> observer.update(book));
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
     * Lấy danh sách sách được đánh dấu "Staff Pick" để hiển thị trên trang chủ.
     */
    @Transactional(readOnly = true)
    public List<Book> findStaffPicks() {
        List<Book> picks = bookRepository.findByIsPickedTrue();
        picks.forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return picks;
    }

    /**
     * Tìm sách theo slug (URL thân thiện).
     */
    @Transactional(readOnly = true)
    public Optional<Book> findBySlug(String slug) {
        Optional<Book> bookOpt = bookRepository.findBySlug(slug);
        bookOpt.ifPresent(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return bookOpt;
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
        Book saved = bookRepository.save(book);
        notifyStockObservers(saved); // Observer Pattern Hook: kích hoạt khi lưu/cập nhật sách
        return saved;
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
        notifyStockObservers(book); // Observer Pattern Hook: kích hoạt khi cập nhật tồn kho
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
