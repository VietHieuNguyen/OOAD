package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

/**
 * Observer Pattern — Observer Interface cho sự kiện thay đổi trạng thái đơn
 * hàng.
 *
 * <p>
 * <b>Kiểu Pull:</b> Observer nhận toàn bộ {@link Order} và trạng thái mới.
 * Observer tự lấy thông tin cần thiết (tên khách hàng, tổng tiền, địa chỉ,
 * ...).
 * </p>
 *
 * <p>
 * <b>Cách thêm Observer mới (OCP):</b> Chỉ cần tạo class mới,
 * đánh dấu {@code @Component}, và {@code implements OrderObserver}.
 * Spring Boot sẽ tự động đăng ký vào {@code OrderService} mà không cần sửa bất
 * kỳ file nào.
 * </p>
 */
public interface OrderObserver {

    /**
     * Được gọi khi trạng thái đơn hàng thay đổi.
     *
     * @param order     Đơn hàng đã được cập nhật trạng thái
     * @param newStatus Trạng thái mới vừa được thiết lập
     */
    void update(Order order, OrderStatus newStatus);
}
