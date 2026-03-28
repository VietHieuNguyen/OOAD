package com.example.bookstore.service.payment;

/**
 * <b>Strategy Pattern — Interface (Strategy)</b>
 *
 * <p>Định nghĩa hợp đồng chung (contract) cho mọi phương thức thanh toán
 * trong hệ thống BookStore.</p>
 *
 * <p><b>Tại sao áp dụng Strategy Pattern ở đây?</b></p>
 * <ul>
 *   <li>Hệ thống cần hỗ trợ nhiều phương thức thanh toán (COD, Chuyển khoản ngân hàng),
 *       mỗi phương thức có logic xử lý khác nhau.</li>
 *   <li>Strategy Pattern cho phép đóng gói từng thuật toán thanh toán vào class riêng biệt,
 *       tuân thủ nguyên lý <b>Open/Closed Principle</b> (dễ dàng thêm phương thức mới
 *       như VNPay, MoMo mà không sửa code cũ).</li>
 *   <li>Tại runtime, hệ thống chọn đúng Strategy dựa trên lựa chọn của người dùng,
 *       thể hiện tính <b>đa hình (Polymorphism)</b> trong OOP.</li>
 * </ul>
 *
 * @see com.example.bookstore.service.payment.CODStrategy
 * @see com.example.bookstore.service.payment.BankTransferStrategy
 * @see com.example.bookstore.service.PaymentService
 */
public interface PaymentStrategy {

    /**
     * Xử lý thanh toán cho một đơn hàng.
     *
     * @param amount Số tiền cần thanh toán (đơn vị: VND hoặc USD tùy cấu hình)
     * @return {@code true} nếu thanh toán thành công, {@code false} nếu thất bại
     */
    boolean processPayment(double amount);
}
