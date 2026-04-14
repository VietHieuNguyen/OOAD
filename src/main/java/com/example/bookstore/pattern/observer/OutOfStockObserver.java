package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Cảnh báo khi sách hết hàng hoàn toàn.
 *
 * <p>Observer này được kích hoạt mỗi khi tồn kho của một cuốn sách thay đổi.
 * Nếu số lượng tồn kho bằng 0, một cảnh báo nghiêm trọng (ERROR level)
 * sẽ được ghi vào log hệ thống.</p>
 *
 * <p><b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<StockObserver>} trong {@code BookService}
 * mà không cần sửa bất kỳ file nào.</p>
 */
@Component
public class OutOfStockObserver implements StockObserver {

    private static final Logger log = LoggerFactory.getLogger(OutOfStockObserver.class);

    /**
     * Ghi cảnh báo ERROR vào log nếu sách đã hết hàng hoàn toàn (stock = 0).
     *
     * @param book Book có tồn kho vừa được cập nhật
     */
    @Override
    public void update(Book book) {
        if (book.getStockQuantity() == 0) {
            log.error("[OUT OF STOCK] Sách \"{}\" (ISBN: {}, ID: {}) đã HẾT HÀNG! Cần nhập thêm ngay.",
                    book.getTitle(), book.getIsbn(), book.getBookId());
        }
    }
}
