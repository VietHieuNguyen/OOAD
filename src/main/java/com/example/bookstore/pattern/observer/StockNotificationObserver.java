package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Notification;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.NotificationType;
import com.example.bookstore.entity.enums.Role;
import com.example.bookstore.repository.NotificationRepository;
import com.example.bookstore.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Lưu thông báo tồn kho vào Database cho Admin.
 *
 * <p>Observer này được kích hoạt mỗi khi tồn kho của một cuốn sách thay đổi.
 * Nếu stock nằm trong ngưỡng nguy hiểm (= 0 hoặc < 10), nó sẽ tạo bản ghi
 * {@link Notification} cho <b>tất cả Admin</b> trong hệ thống.</p>
 *
 * <p><b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<StockObserver>} trong {@code BookService}
 * cùng với {@link LowStockAlertObserver} và {@link OutOfStockObserver},
 * mà <b>không cần sửa bất kỳ file nào</b>.</p>
 */
@Component
public class StockNotificationObserver implements StockObserver {

    private static final Logger log = LoggerFactory.getLogger(StockNotificationObserver.class);

    /** Ngưỡng tồn kho thấp — giống LowStockAlertObserver. */
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public StockNotificationObserver(NotificationRepository notificationRepository,
                                     UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tạo Notification và lưu vào DB khi stock ở mức nguy hiểm.
     *
     * <ul>
     *   <li>Stock = 0 → tạo thông báo {@code OUT_OF_STOCK_ALERT} cho tất cả Admin.</li>
     *   <li>0 < Stock < 10 → tạo thông báo {@code LOW_STOCK_ALERT} cho tất cả Admin.</li>
     *   <li>Stock ≥ 10 → bỏ qua (không tạo thông báo).</li>
     * </ul>
     *
     * @param book Book có tồn kho vừa được cập nhật
     */
    @Override
    public void update(Book book) {
        int stock = book.getStockQuantity();

        NotificationType type;
        String title;
        String message;

        if (stock == 0) {
            type = NotificationType.OUT_OF_STOCK_ALERT;
            title = "Book OUT OF STOCK";
            message = String.format(
                    "Book \"%s\" (ID: %s) is completely out of stock. Restock needed immediately!",
                    book.getTitle(), book.getBookId());
        } else if (stock > 0 && stock < LOW_STOCK_THRESHOLD) {
            type = NotificationType.LOW_STOCK_ALERT;
            title = "Book running low";
            message = String.format(
                    "Book \"%s\" (ID: %s) has only %d units left. Consider restocking.",
                    book.getTitle(), book.getBookId(), stock);
        } else {
            return; // Stock >= 10 → không cần thông báo
        }

        // Tìm tất cả Admin trong hệ thống
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        if (admins.isEmpty()) {
            log.warn("[DB NOTIFICATION] Không tìm thấy Admin nào trong hệ thống!");
            return;
        }

        // Tạo 1 Notification cho mỗi Admin
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRelatedBookId(book.getBookId());

            notificationRepository.save(notification);
        }

        log.info("[DB NOTIFICATION] Đã lưu thông báo '{}' cho {} Admin | Sách: \"{}\" | Stock: {}",
                type, admins.size(), book.getTitle(), stock);
    }
}
