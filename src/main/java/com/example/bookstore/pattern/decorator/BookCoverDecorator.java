package com.example.bookstore.pattern.decorator;

import java.math.BigDecimal;

/**
 * <b>Concrete Decorator 3 — Phí bọc bìa sách</b>
 *
 * <p>Decorator này cộng thêm phí bọc bìa vào tổng giá giỏ hàng.
 * Phí được tính theo số quyển sách: mỗi quyển tốn thêm
 * {@value #FEE_PER_BOOK} VNĐ để bọc bìa bảo vệ.</p>
 *
 * <p><b>Vai trò trong Design Pattern:</b> Concrete Decorator</p>
 *
 * <p><b>Các lỗi kinh điển đã tránh:</b></p>
 * <ul>
 *   <li>Clip {@code bookCount >= 0} bằng {@code Math.max} để tránh phí âm.</li>
 *   <li>Sử dụng {@link BigDecimal} thay vì {@code double} để tránh sai số
 *       thập phân trong tính tiền.</li>
 *   <li>Không mutate state của wrappee — chỉ đọc và cộng thêm.</li>
 * </ul>
 *
 * @see CartPricerDecorator
 * @see CartPricer
 */
public class BookCoverDecorator extends CartPricerDecorator {

    /** Phí bọc bìa cho mỗi quyển sách (5.000 VNĐ). */
    private static final BigDecimal FEE_PER_BOOK = new BigDecimal("5000");

    private final int coveredBookCount;

    /**
     * Khởi tạo BookCoverDecorator.
     *
     * @param wrappee          Đối tượng CartPricer bên trong
     * @param coveredBookCount Số lượng quyển sách được chọn bọc bìa
     */
    public BookCoverDecorator(CartPricer wrappee, int coveredBookCount) {
        super(wrappee);
        this.coveredBookCount = Math.max(coveredBookCount, 0); // Không cho âm
    }

    /**
     * Tính giá = giá wrappee + (số quyển × phí bọc mỗi quyển).
     */
    @Override
    public BigDecimal calculatePrice() {
        BigDecimal basePrice = wrappee.calculatePrice();
        BigDecimal coverFee = FEE_PER_BOOK.multiply(BigDecimal.valueOf(coveredBookCount));
        return basePrice.add(coverFee);
    }

    @Override
    public String getDescription() {
        return wrappee.getDescription() + " + Phí bọc bìa sách";
    }

    /**
     * Lấy tổng phí bọc bìa (để hiển thị trên UI).
     */
    public BigDecimal getCoverFee() {
        return FEE_PER_BOOK.multiply(BigDecimal.valueOf(coveredBookCount));
    }
}
