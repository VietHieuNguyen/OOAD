package com.example.bookstore.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.example.bookstore.entity.enums.PaymentMethodType;


/**
 * <b>Strategy Pattern — Concrete Strategy B: Chuyển khoản ngân hàng</b>
 *
 * <p>Implement {@link PaymentStrategy} cho phương thức Bank Transfer.
 * Trong thực tế, class này sẽ tích hợp với API ngân hàng (VietcomBank, BIDV...)
 * hoặc cổng thanh toán (VNPay, Momo) để tạo mã QR và xác nhận giao dịch.</p>
 *
 * <p>Trong đồ án này, logic được giả lập bằng {@code Thread.sleep()}
 * để mô phỏng độ trễ khi gọi API ngân hàng.</p>
 *
 * @see PaymentStrategy
 */
@Component
public class BankTransferStrategy implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BankTransferStrategy.class);

    /**
     * Xử lý thanh toán chuyển khoản ngân hàng.
     * <p>Giả lập quá trình gọi API ngân hàng để khởi tạo giao dịch chuyển khoản.</p>
     *
     * @param amount Số tiền cần chuyển khoản
     * @return {@code true} — giao dịch được khởi tạo thành công (chờ admin xác nhận)
     */
    @Override
    public boolean processPayment(double amount) {
        try {
            logger.info("[BANK] Đang kết nối đến cổng thanh toán ngân hàng...");
            // Giả lập độ trễ gọi API ngân hàng (1000ms)
            Thread.sleep(1000);
            logger.info("[BANK] Đang xử lý giao dịch chuyển khoản: {} ...", amount);
            Thread.sleep(500);
            logger.info("[BANK] ✓ Giao dịch chuyển khoản đã được khởi tạo. Số tiền: {}. Chờ xác nhận từ ngân hàng.", amount);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[BANK] ✗ Lỗi khi kết nối cổng thanh toán ngân hàng", e);
            return false;
        }
    }

    /**
     * Khai báo định danh của Strategy này là {@link PaymentMethodType#BANK_TRANSFER}.
     * Spring sẽ dùng giá trị này để tự động đăng ký vào Map trong Context.
     */
    @Override
    public PaymentMethodType getSupportedMethod() {
        return PaymentMethodType.BANK_TRANSFER;
    }
}
