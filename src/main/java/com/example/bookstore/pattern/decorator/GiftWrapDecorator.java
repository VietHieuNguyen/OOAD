package com.example.bookstore.pattern.decorator;

import java.math.BigDecimal;

/**
 * <b>Concrete Decorator 1 — Phí gói quà tặng</b>
 *
 * <p>Decorator này cộng thêm phí gói quà vào tổng giá giỏ hàng.
 * Phí được tính theo số lượng sản phẩm: mỗi sản phẩm tốn thêm
 * {@value #FEE_PER_ITEM} VNĐ để gói quà.</p>
 *
 * <p><b>Vai trò trong Design Pattern:</b> Concrete Decorator</p>
 *
 * <p><b>Các lỗi kinh điển đã tránh:</b></p>
 * <ul>
 *   <li>Validate {@code itemCount > 0} để tránh phí = 0 khi giỏ trống.</li>
 *   <li>Sử dụng {@link BigDecimal} thay vì {@code double} để tránh sai số
 *       thập phân trong tính tiền.</li>
 *   <li>Không mutate state của wrappee — chỉ đọc và cộng thêm.</li>
 * </ul>
 *
 * @see CartPricerDecorator
 * @see CartPricer
 */
public class GiftWrapDecorator extends CartPricerDecorator {

    /** Phí gói quà cho mỗi sản phẩm (20.000 VNĐ). */
    private static final BigDecimal FEE_PER_ITEM = new BigDecimal("20000");

    private final int itemCount;

    /**
     * Khởi tạo GiftWrapDecorator.
     *
     * @param wrappee   Đối tượng CartPricer bên trong
     * @param itemCount Số lượng sản phẩm cần gói quà
     */
    public GiftWrapDecorator(CartPricer wrappee, int itemCount) {
        super(wrappee);
        this.itemCount = Math.max(itemCount, 0); // Không cho âm
    }

    /**
     * Tính giá = giá wrappee + (số sản phẩm × phí gói mỗi sản phẩm).
     */
    @Override
    public BigDecimal calculatePrice() {
        BigDecimal basePrice = wrappee.calculatePrice();
        BigDecimal wrapFee = FEE_PER_ITEM.multiply(BigDecimal.valueOf(itemCount));
        return basePrice.add(wrapFee);
    }

    @Override
    public String getDescription() {
        return wrappee.getDescription() + " + Phí gói quà";
    }

    /**
     * Lấy tổng phí gói quà (để hiển thị trên UI).
     */
    public BigDecimal getWrapFee() {
        return FEE_PER_ITEM.multiply(BigDecimal.valueOf(itemCount));
    }
}
