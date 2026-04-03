package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Book;

/**
 * Observer Pattern — Subject Interface cho sự kiện tồn kho.
 *
 * <p>Bất kỳ class nào muốn phát sự kiện "tồn kho thay đổi" đều implement interface này.
 * Trong hệ thống, {@link com.example.bookstore.service.BookService} sẽ implement.</p>
 *
 * <p>Danh sách Observer được quản lý nội bộ trong Subject;
 * bên ngoài chỉ cần gọi {@link #notifyStockObservers(Book)}.</p>
 */
public interface StockSubject {

    /** Đăng ký một Observer mới vào danh sách lắng nghe. */
    void addStockObserver(StockObserver observer);

    /** Hủy đăng ký một Observer khỏi danh sách lắng nghe. */
    void removeStockObserver(StockObserver observer);

    /**
     * Thông báo tới tất cả Observer đã đăng ký.
     *
     * @param book Book có tồn kho vừa thay đổi
     */
    void notifyStockObservers(Book book);
}
