package com.example.bookstore.pattern.decorator;

import java.math.BigDecimal;

/**
 * <b>Decorator (Abstract) — Decorator Pattern</b>
 *
 * <p>Lớp trừu tượng đóng vai trò Decorator trong mẫu thiết kế.
 * Chứa một tham chiếu tới {@link CartPricer} (wrappee) và delegate
 * mọi lời gọi tới đối tượng được bọc bên trong.</p>
 *
 * <p><b>Tại sao cần lớp Abstract Decorator?</b></p>
 * <ul>
 *   <li>Tránh lặp code: Tất cả Concrete Decorator đều cần giữ reference
 *       tới wrappee và delegate — logic này được tập trung ở đây.</li>
 *   <li>Đảm bảo tính nhất quán: Mọi decorator đều phải gọi {@code super(wrappee)}
 *       trong constructor, tránh quên bước khởi tạo.</li>
 *   <li>Tuân thủ Open-Closed Principle: Thêm decorator mới chỉ cần
 *       extend class này mà không cần sửa code cũ.</li>
 * </ul>
 *
 * <p><b>Vai trò trong Design Pattern:</b> Decorator (Base Decorator)</p>
 *
 * @see CartPricer
 * @see BaseCartPricer
 * @see GiftWrapDecorator
 * @see VoucherDiscountDecorator
 */
public abstract class CartPricerDecorator implements CartPricer {

    /**
     * Đối tượng CartPricer được bọc bên trong (wrappee).
     * Có thể là BaseCartPricer hoặc một Decorator khác (nested).
     */
    protected final CartPricer wrappee;

    /**
     * Khởi tạo decorator với đối tượng CartPricer cần bọc.
     *
     * @param wrappee Đối tượng CartPricer bên trong
     * @throws IllegalArgumentException nếu wrappee là null
     */
    protected CartPricerDecorator(CartPricer wrappee) {
        if (wrappee == null) {
            throw new IllegalArgumentException("Wrappee CartPricer không được null.");
        }
        this.wrappee = wrappee;
    }

    /**
     * Delegate tính giá cho wrappee.
     * Các subclass sẽ override để thêm/bớt giá.
     */
    @Override
    public BigDecimal calculatePrice() {
        return wrappee.calculatePrice();
    }

    /**
     * Delegate mô tả cho wrappee.
     * Các subclass sẽ override để bổ sung mô tả.
     */
    @Override
    public String getDescription() {
        return wrappee.getDescription();
    }
}
