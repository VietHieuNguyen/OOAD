package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Book;

/**
 * Observer Pattern — Observer Interface cho sự kiện thay đổi tồn kho.
 *
 * <p>Mỗi class implement interface này sẽ được gọi tự động khi
 * tồn kho của một cuốn sách thay đổi (thông qua {@code BookService}).</p>
 *
 * <p><b>Kiểu Pull:</b> Observer nhận toàn bộ đối tượng {@link Book}
 * và tự quyết định lấy dữ liệu nào cần dùng.</p>
 *
 * <p><b>Cách thêm Observer mới (OCP):</b> Chỉ cần tạo class mới,
 * đánh dấu {@code @Component}, và {@code implements StockObserver}.
 * Spring Boot sẽ tự động đăng ký vào hệ thống mà không cần sửa bất kỳ file nào khác.</p>
 */
public interface StockObserver {

    /**
     * Được gọi khi tồn kho của một cuốn sách thay đổi.
     *
     * @param book Book đã được cập nhật (có {@code stockQuantity} mới nhất)
     */
    void update(Book book);
}
