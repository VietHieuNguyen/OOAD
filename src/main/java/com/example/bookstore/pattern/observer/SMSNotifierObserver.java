package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Giả lập gửi SMS khi đơn hàng đổi trạng thái.
 *
 * <p>Trong môi trường production, class này sẽ tích hợp với SMS gateway
 * (Twilio, ESMS,...). Hiện tại chỉ ghi log để mô phỏng hành vi gửi SMS.</p>
 *
 * <p><b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<OrderObserver>} trong {@code OrderService}
 * mà không cần sửa bất kỳ file nào.</p>
 */
@Component
public class SMSNotifierObserver implements OrderObserver {

    private static final Logger log = LoggerFactory.getLogger(SMSNotifierObserver.class);

    /**
     * Giả lập gửi SMS thông báo ngắn gọn khi trạng thái đơn hàng thay đổi.
     *
     * <p>SMS chỉ gửi cho các trạng thái quan trọng ({@code CONFIRMED}, {@code COMPLETED},
     * {@code CANCELLED}) để tránh spam tin nhắn.</p>
     *
     * @param order     Đơn hàng đã được cập nhật
     * @param newStatus Trạng thái mới của đơn hàng
     */
    @Override
    public void update(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.PENDING) {
            return; // Không gửi SMS khi đơn mới tạo
        }

        String customerName = order.getCustomer().getFullName();
        String smsContent   = buildSmsContent(order, newStatus);

        log.info("[SMS] Gửi SMS tới {} | Đơn #{} | Nội dung: {}",
                customerName, order.getOrderId(), smsContent);
    }

    private String buildSmsContent(Order order, OrderStatus status) {
        String orderShort = order.getOrderId().substring(0, 8).toUpperCase();
        return switch (status) {
            case CONFIRMED -> String.format("BookStore: Don hang #%s da duoc xac nhan. Cam on ban!", orderShort);
            case COMPLETED -> String.format("BookStore: Don hang #%s da hoan thanh. Chuc ban doc sach vui!", orderShort);
            case CANCELLED -> String.format("BookStore: Don hang #%s da bi huy. Lien he CSKH de biet them.", orderShort);
            default        -> String.format("BookStore: Don hang #%s co trang thai moi: %s.", orderShort, status.name());
        };
    }
}
