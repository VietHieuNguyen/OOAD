package com.example.bookstore.pattern.decorator;

import java.math.BigDecimal;

/**
 * <b>Component Interface — Decorator Pattern</b>
 *
 * <p>Định nghĩa giao diện chung cho việc tính giá giỏ hàng.
 * Tất cả các Concrete Component và Decorator đều phải implement interface này.</p>
 *
 * <p><b>Vai trò trong Design Pattern:</b> Component</p>
 *
 * @see BaseCartPricer
 * @see CartPricerDecorator
 */
public interface CartPricer {

    /**
     * Tính tổng giá của giỏ hàng (sau khi áp dụng các decorator).
     *
     * @return Tổng tiền dạng BigDecimal
     */
    BigDecimal calculatePrice();

    /**
     * Mô tả chi tiết các thành phần giá đã được áp dụng.
     *
     * @return Chuỗi mô tả (vd: "Tạm tính + Phí gói quà - Mã giảm giá GIAM10")
     */
    String getDescription();
}
