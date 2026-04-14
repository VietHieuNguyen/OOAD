package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.entity.enums.OrderStatus;
import com.example.bookstore.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Điều chỉnh tồn kho khi đơn hàng đổi trạng thái.
 *
 * <ul>
 *   <li><b>CONFIRMED</b> → Trừ {@code stockQuantity} theo số lượng từng {@link OrderItem}.</li>
 *   <li><b>CANCELLED</b> → Cộng lại {@code stockQuantity} (hoàn kho).</li>
 *   <li>Các trạng thái khác → Bỏ qua.</li>
 * </ul>
 *
 * <p><b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<OrderObserver>} trong {@code OrderService}
 * mà <b>không cần sửa bất kỳ file nào</b>.</p>
 *
 * <p><b>Event Chain (Chuỗi sự kiện):</b> Observer này tạo ra một chuỗi sự kiện tự nhiên:
 * <pre>
 *   OrderService (OrderStatusSubject)
 *     → StockAdjustmentObserver (OrderObserver) — trừ/cộng kho
 *       → BookService (StockSubject) — notifyStockObservers()
 *         → LowStockAlertObserver, OutOfStockObserver, StockNotificationObserver
 * </pre>
 * Không có vòng lặp phụ thuộc (OrderService → Observer → BookService → StockObservers),
 * và mỗi Observer vẫn giữ đúng Single Responsibility.</p>
 */
@Component
public class StockAdjustmentObserver implements OrderObserver {

    private static final Logger log = LoggerFactory.getLogger(StockAdjustmentObserver.class);

    /**
     * Inject BookService (StockSubject) thay vì BookRepository trực tiếp.
     * Lý do: BookService.updateStock() sẽ gọi notifyStockObservers() sau khi save,
     * giúp các StockObserver (LowStock, OutOfStock, StockNotification) được kích hoạt.
     * Nếu gọi thẳng BookRepository.save() → bypass Subject → StockObserver không bao giờ chạy.
     */
    private final BookService bookService;

    public StockAdjustmentObserver(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Điều chỉnh tồn kho khi đơn hàng chuyển sang CONFIRMED hoặc CANCELLED.
     *
     * @param order     Đơn hàng vừa được cập nhật trạng thái
     * @param newStatus Trạng thái mới của đơn hàng
     */
    @Override
    public void update(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CONFIRMED) {
            deductStock(order);
        } else if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        // PENDING, COMPLETED → không tác động đến tồn kho
    }

    /**
     * Trừ tồn kho khi đơn được XÁC NHẬN.
     * Gọi qua BookService.updateStock() để kích hoạt chuỗi StockObserver.
     */
    private void deductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            String bookId = item.getBook().getBookId();
            int qty = item.getQuantity();

            bookService.findById(bookId).ifPresent(book -> {
                int currentStock = book.getStockQuantity();
                int newStock = Math.max(0, currentStock - qty);

                if (currentStock < qty) {
                    log.warn("[STOCK ADJUSTMENT] Tồn kho không đủ cho sách \"{}\" | Cần: {} | Còn: {} → đặt về 0",
                            book.getTitle(), qty, currentStock);
                }

                // Gọi BookService.updateStock() → trigger notifyStockObservers()
                // → LowStockAlertObserver, OutOfStockObserver, StockNotificationObserver tự động chạy
                bookService.updateStock(bookId, newStock);
                log.info("[STOCK ADJUSTMENT] CONFIRMED đơn #{} → Trừ kho sách \"{}\" | {} - {} = {}",
                        order.getOrderId().substring(0, 8).toUpperCase(),
                        book.getTitle(), currentStock, qty, newStock);
            });
        }
    }

    /**
     * Cộng lại tồn kho khi đơn bị HỦY (hoàn kho).
     * Gọi qua BookService.updateStock() để kích hoạt chuỗi StockObserver.
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            String bookId = item.getBook().getBookId();
            int qty = item.getQuantity();

            bookService.findById(bookId).ifPresent(book -> {
                int currentStock = book.getStockQuantity();
                int newStock = currentStock + qty;

                // Gọi BookService.updateStock() → trigger notifyStockObservers()
                bookService.updateStock(bookId, newStock);
                log.info("[STOCK ADJUSTMENT] CANCELLED đơn #{} → Hoàn kho sách \"{}\" | {} + {} = {}",
                        order.getOrderId().substring(0, 8).toUpperCase(),
                        book.getTitle(), currentStock, qty, newStock);
            });
        }
    }
}
