package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Notification;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.NotificationType;
import com.example.bookstore.entity.enums.OrderStatus;
import com.example.bookstore.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Lưu thông báo đơn hàng vào Database.
 *
 * <p>Observer này được kích hoạt mỗi khi Admin chuyển trạng thái đơn hàng.
 * Nó tạo một bản ghi {@link Notification} trong bảng {@code notifications}
 * với người nhận là <b>Customer</b> (chủ đơn hàng).</p>
 *
 * <p><b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<OrderObserver>} trong {@code OrderService}
 * cùng với {@link EmailNotifierObserver} và {@link SMSNotifierObserver},
 * mà <b>không cần sửa bất kỳ file nào</b>.</p>
 *
 * <p><b>Đây là minh chứng trực quan nhất cho Open/Closed Principle:</b>
 * Thêm tính năng "lưu thông báo vào DB" chỉ bằng cách tạo file mới.</p>
 */
@Component
public class OrderNotificationObserver implements OrderObserver {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationObserver.class);

    private final NotificationRepository notificationRepository;

    public OrderNotificationObserver(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Tạo Notification và lưu vào DB khi đơn hàng đổi trạng thái.
     *
     * <p>Bỏ qua trạng thái {@code PENDING} (đơn mới tạo — xử lý riêng).</p>
     *
     * @param order     Đơn hàng đã được cập nhật
     * @param newStatus Trạng thái mới của đơn hàng
     */
    @Override
    public void update(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.PENDING) {
            return;
        }

        NotificationType type = mapStatusToType(newStatus);
        String title = buildTitle(newStatus);
        String message = buildMessage(order, newStatus);

        Notification notification = new Notification();
        notification.setUser(order.getCustomer());  // Gửi cho Customer (chủ đơn)
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedOrderId(order.getOrderId());

        notificationRepository.save(notification);
        log.info("[DB NOTIFICATION] Đã lưu thông báo cho Customer '{}' | Đơn #{} | Loại: {}",
                order.getCustomer().getFullName(), order.getOrderId(), type);
    }

    /**
     * Map OrderStatus → NotificationType.
     */
    private NotificationType mapStatusToType(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.ORDER_CONFIRMED;
            case COMPLETED -> NotificationType.ORDER_COMPLETED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.ORDER_CONFIRMED; // fallback
        };
    }

    private String buildTitle(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Order confirmed";
            case COMPLETED -> "Order completed";
            case CANCELLED -> "Order cancelled";
            default -> "Order update";
        };
    }

    private String buildMessage(Order order, OrderStatus status) {
        String orderId = order.getOrderId().substring(0, 8).toUpperCase();
        return switch (status) {
            case CONFIRMED -> String.format(
                    "Your order #%s (total: %s VND) has been confirmed and is being processed.",
                    orderId, order.getTotalAmount().toPlainString());
            case COMPLETED -> String.format(
                    "Order #%s has been completed. Thank you for shopping at BookStore!",
                    orderId);
            case CANCELLED -> String.format(
                    "Order #%s has been cancelled. Please contact customer support if you need assistance.",
                    orderId);
            default -> String.format("Order #%s has new updates.", orderId);
        };
    }
}
