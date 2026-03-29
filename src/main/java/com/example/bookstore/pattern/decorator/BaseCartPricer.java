package com.example.bookstore.pattern.decorator;

import com.example.bookstore.entity.CartItem;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * <b>Concrete Component — Decorator Pattern</b>
 *
 * <p>Tính tổng giá cơ sở (base price) của giỏ hàng bằng cách
 * cộng dồn {@code unitPrice × quantity} của từng {@link CartItem}.</p>
 *
 * <p><b>Vai trò trong Design Pattern:</b> Concrete Component —
 * đây là đối tượng gốc chưa được "trang trí" (decorate) bởi bất kỳ
 * lớp bọc nào.</p>
 *
 * @see CartPricer
 * @see CartPricerDecorator
 */
public class BaseCartPricer implements CartPricer {

    private final Collection<CartItem> items;

    /**
     * Khởi tạo BaseCartPricer với danh sách CartItem.
     *
     * @param items Danh sách sản phẩm trong giỏ hàng
     * @throws IllegalArgumentException nếu items là null
     */
    public BaseCartPricer(Collection<CartItem> items) {
        if (items == null) {
            throw new IllegalArgumentException("Danh sách CartItem không được null.");
        }
        this.items = items;
    }

    /**
     * Tính tổng giá cơ sở: Σ(unitPrice × quantity) cho mỗi CartItem.
     *
     * @return Tổng tiền cơ sở (chưa áp dụng giảm giá hay phụ phí)
     */
    @Override
    public BigDecimal calculatePrice() {
        return items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String getDescription() {
        return "Tạm tính";
    }
}
