package com.example.bookstore.repository;

import com.example.bookstore.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho Entity {@link Notification}.
 *
 * <p>Cung cấp các query cơ bản phục vụ cho Observer Pattern (lưu thông báo)
 * và chuẩn bị sẵn cho giao diện chuông thông báo (giai đoạn sau).</p>
 */
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Lấy tất cả thông báo của 1 user, sắp xếp theo thời gian mới nhất.
     * Dùng cho giao diện xem danh sách thông báo (giai đoạn sau).
     *
     * @param userId ID của user (Customer hoặc Admin)
     * @return Danh sách thông báo sắp xếp DESC theo createdAt
     */
    List<Notification> findByUser_IdOrderByCreatedAtDesc(String userId);

    /**
     * Đếm số thông báo chưa đọc của 1 user.
     * Dùng cho badge chuông thông báo (giai đoạn sau).
     *
     * @param userId ID của user
     * @return Số lượng thông báo chưa đọc
     */
    long countByUser_IdAndIsReadFalse(String userId);
}
