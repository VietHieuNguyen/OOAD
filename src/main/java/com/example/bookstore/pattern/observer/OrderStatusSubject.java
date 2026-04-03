package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

/**
 * Observer Pattern — Subject Interface cho sự kiện trạng thái đơn hàng.
 *
 * <p>
 * {@link com.example.bookstore.service.OrderService} sẽ implement interface
 * này.
 * Khi admin chuyển trạng thái đơn hàng,
 * {@link #notifyOrderObservers(Order, OrderStatus)}
 * sẽ được gọi để kích hoạt toàn bộ Observer đã đăng ký.
 * </p>
 */
public interface OrderStatusSubject {

    /** Đăng ký một Observer mới vào danh sách lắng nghe. */
    void addOrderObserver(OrderObserver observer);

    /** Hủy đăng ký một Observer khỏi danh sách lắng nghe. */
    void removeOrderObserver(OrderObserver observer);

    /**
     * Thông báo tới tất cả Observer đã đăng ký.
     *
     * @param order     Đơn hàng vừa được cập nhật
     * @param newStatus Trạng thái mới của đơn hàng
     */
    void notifyOrderObservers(Order order, OrderStatus newStatus);
}
