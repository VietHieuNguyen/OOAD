package com.example.bookstore.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <b>Strategy Pattern — Concrete Strategy A: Thanh toán khi nhận hàng (COD)</b>
 *
 * <p>Implement {@link PaymentStrategy} cho phương thức Cash On Delivery.
 * Với COD, khách hàng thanh toán trực tiếp cho shipper khi nhận được hàng,
 * do đó hệ thống chỉ cần xác nhận đơn hàng hợp lệ và ghi nhận yêu cầu.</p>
 *
 * <p>Logic được giả lập bằng {@code Thread.sleep()} để mô phỏng độ trễ
 * của quá trình xác thực đơn hàng trong môi trường thực tế.</p>
 *
 * @see PaymentStrategy
 */
@Component
public class CODStrategy implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CODStrategy.class);

    /**
     * Xử lý thanh toán COD.
     * <p>Giả lập quá trình xác thực đơn hàng COD (kiểm tra địa chỉ, vùng giao hàng...).</p>
     *
     * @param amount Số tiền đơn hàng
     * @return {@code true} — COD luôn chấp nhận (thu tiền khi giao hàng)
     */
    @Override
    public boolean processPayment(double amount) {
        try {
            logger.info("[COD] Đang xác nhận đơn hàng COD với số tiền: {} ...", amount);
            // Giả lập độ trễ xác thực đơn hàng (500ms)
            Thread.sleep(500);
            logger.info("[COD] ✓ Đơn hàng COD đã được xác nhận. Thu tiền khi giao hàng: {}", amount);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[COD] ✗ Lỗi khi xử lý đơn COD", e);
            return false;
        }
    }
}
