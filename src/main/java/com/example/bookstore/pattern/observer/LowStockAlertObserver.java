package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Cảnh báo khi sách sắp hết hàng.
 *
 * <p>Observer này được kích hoạt mỗi khi tồn kho của một cuốn sách thay đổi.
 * Nếu số lượng tồn kho nằm trong ngưỡng cảnh báo ({@value #LOW_STOCK_THRESHOLD}),
 * một cảnh báo sẽ được ghi vào log hệ thống.</p>
 *
 * <p><b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<StockObserver>} trong {@code BookService}
 * mà không cần sửa bất kỳ file nào.</p>
 */
@Component
public class LowStockAlertObserver implements StockObserver {

    private static final Logger log = LoggerFactory.getLogger(LowStockAlertObserver.class);

    /** Ngưỡng tồn kho thấp — cảnh báo khi stock < giá trị này. */
    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * Ghi cảnh báo vào log nếu sách sắp hết hàng (0 < stock < 10).
     *
     * @param book Book có tồn kho vừa được cập nhật
     */
    @Override
    public void update(Book book) {
        int stock = book.getStockQuantity();
        if (stock > 0 && stock < LOW_STOCK_THRESHOLD) {
            log.warn("[STOCK ALERT] Sách \"{}\" (ID: {}) sắp hết hàng! Còn lại: {} cuốn.",
                    book.getTitle(), book.getBookId(), stock);
        }
    }
}
