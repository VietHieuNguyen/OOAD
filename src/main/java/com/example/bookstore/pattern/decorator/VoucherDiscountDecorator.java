package com.example.bookstore.pattern.decorator;

import com.example.bookstore.entity.Voucher;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <b>Concrete Decorator 2 — Mã giảm giá (Voucher)</b>
 *
 * <p>Decorator này giảm giá theo phần trăm dựa trên thông tin
 * từ Entity {@link Voucher}. Số tiền giảm bị giới hạn bởi
 * {@code Voucher.maxDiscountAmount} (nếu có).</p>
 *
 * <p><b>Vai trò trong Design Pattern:</b> Concrete Decorator</p>
 *
 * <p><b>Các lỗi kinh điển đã tránh:</b></p>
 * <ul>
 *   <li>Validate voucher hợp lệ ({@link Voucher#isValid()}) trước khi áp dụng.</li>
 *   <li>Giá sau giảm không bao giờ âm — dùng {@code BigDecimal.max(result, ZERO)}.</li>
 *   <li>Giới hạn giảm tối đa ({@code maxDiscountAmount}) để tránh exploit.</li>
 *   <li>Sử dụng {@link RoundingMode#HALF_UP} khi tính phần trăm để tránh
 *       lỗi ArithmeticException với số thập phân vô hạn.</li>
 * </ul>
 *
 * @see CartPricerDecorator
 * @see Voucher
 */
public class VoucherDiscountDecorator extends CartPricerDecorator {

    private final Voucher voucher;

    /**
     * Khởi tạo VoucherDiscountDecorator.
     *
     * @param wrappee Đối tượng CartPricer bên trong
     * @param voucher Entity Voucher chứa thông tin giảm giá
     * @throws IllegalArgumentException nếu voucher là null
     */
    public VoucherDiscountDecorator(CartPricer wrappee, Voucher voucher) {
        super(wrappee);
        if (voucher == null) {
            throw new IllegalArgumentException("Voucher không được null.");
        }
        this.voucher = voucher;
    }

    /**
     * Tính giá = giá wrappee - tiền giảm.
     *
     * <p>Tiền giảm = basePrice × (discountPercentage / 100),
     * nhưng tối đa không vượt quá {@code maxDiscountAmount}.</p>
     *
     * <p>Nếu voucher không hợp lệ (hết hạn, hết lượt, bị vô hiệu),
     * trả về giá wrappee nguyên vẹn mà không giảm.</p>
     */
    @Override
    public BigDecimal calculatePrice() {
        BigDecimal basePrice = wrappee.calculatePrice();

        // Nếu voucher không hợp lệ → trả giá gốc, không giảm
        if (!voucher.isValid()) {
            return basePrice;
        }

        // Tính tiền giảm theo phần trăm
        BigDecimal discountRate = BigDecimal.valueOf(voucher.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = basePrice.multiply(discountRate)
                .setScale(0, RoundingMode.HALF_UP);

        // Giới hạn giảm tối đa (nếu có)
        if (voucher.getMaxDiscountAmount() != null
                && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discountAmount = voucher.getMaxDiscountAmount();
        }

        // Đảm bảo giá không bao giờ âm
        BigDecimal finalPrice = basePrice.subtract(discountAmount);
        return finalPrice.max(BigDecimal.ZERO);
    }

    @Override
    public String getDescription() {
        if (!voucher.isValid()) {
            return wrappee.getDescription();
        }
        return wrappee.getDescription() + " - Mã giảm giá " + voucher.getCode();
    }

    /**
     * Lấy số tiền giảm (để hiển thị trên UI).
     */
    public BigDecimal getDiscountAmount() {
        BigDecimal basePrice = wrappee.calculatePrice();
        if (!voucher.isValid()) return BigDecimal.ZERO;

        BigDecimal discountRate = BigDecimal.valueOf(voucher.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = basePrice.multiply(discountRate)
                .setScale(0, RoundingMode.HALF_UP);

        if (voucher.getMaxDiscountAmount() != null
                && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discountAmount = voucher.getMaxDiscountAmount();
        }
        return discountAmount;
    }
}
