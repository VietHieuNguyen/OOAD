package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.Voucher;
import com.example.bookstore.pattern.decorator.BaseCartPricer;
import com.example.bookstore.pattern.decorator.BookCoverDecorator;
import com.example.bookstore.pattern.decorator.CartPricer;
import com.example.bookstore.pattern.decorator.GiftWrapDecorator;
import com.example.bookstore.pattern.decorator.VoucherDiscountDecorator;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.VoucherRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý giỏ hàng.
 *
 * <p>Tích hợp <b>Decorator Pattern</b> để tính giá giỏ hàng linh hoạt.
 * Phương thức {@link #calculatePriceBreakdown(Cart)} sử dụng chuỗi Decorator
 * lồng nhau: BaseCartPricer → GiftWrapDecorator → BookCoverDecorator → VoucherDiscountDecorator.</p>
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final VoucherRepository voucherRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       BookRepository bookRepository,
                       VoucherRepository voucherRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.voucherRepository = voucherRepository;
    }

    /**
     * Lấy giỏ hàng hiện tại của khách hàng, hoặc tạo mới nếu chưa có.
     */
    @Transactional
    public Cart getOrCreateCart(Customer customer) {
        Cart cart = cartRepository.findByCustomer_Id(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
        // Force init lazy-loaded collections (open-in-view=false)
        cart.getItems().forEach(item -> {
            item.getBook().getTitle(); // init Book
            if (item.getBook().getCategory() != null) {
                item.getBook().getCategory().getName(); // init Category
            }
        });
        if (cart.getAppliedVoucher() != null) {
            cart.getAppliedVoucher().getCode(); // init Voucher
        }
        return cart;
    }

    /**
     * Xóa các sản phẩm TRONG giỏ hàng ĐÃ ĐƯỢC CHỌN sau khi đặt hàng thành công.
     */
    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().removeIf(CartItem::getIsSelected);
        cart.setAppliedVoucher(null);
        cartRepository.save(cart);
    }

    /**
     * Thêm sách vào giỏ hàng. Nếu sách đã có trong giỏ, tăng số lượng.
     */
    @Transactional
    public void addCartItem(Customer customer, String bookId, int quantity) {
        Cart cart = getOrCreateCart(customer);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        if (quantity <= 0) quantity = 1;
        if (book.getStockQuantity() < quantity) {
            throw new IllegalStateException("Book is out of stock.");
        }

        // Kiểm tra xem sách đã có trong giỏ chưa
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(book.getPrice());
            cart.getItems().add(newItem);
            cartRepository.save(cart);
        }
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng.
     */
    @Transactional
    public void removeCartItem(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ.
     */
    @Transactional
    public void updateQuantity(String cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart."));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    /**
     * Áp dụng mã giảm giá vào giỏ hàng.
     *
     * @return Thông báo kết quả (thành công hoặc lỗi)
     */
    @Transactional
    public String applyVoucher(Cart cart, String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return "Vui lòng nhập mã giảm giá.";
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByCode(voucherCode.trim().toUpperCase());
        if (voucherOpt.isEmpty()) {
            return "Mã giảm giá không tồn tại.";
        }

        Voucher voucher = voucherOpt.get();
        if (!voucher.isValid()) {
            return "Mã giảm giá đã hết hạn hoặc hết lượt sử dụng.";
        }

        cart.setAppliedVoucher(voucher);
        cartRepository.save(cart);
        return null; // null = thành công
    }

    /**
     * Hủy mã giảm giá đang áp dụng.
     */
    @Transactional
    public void removeVoucher(Cart cart) {
        cart.setAppliedVoucher(null);
        cartRepository.save(cart);
    }

    /**
     * Bật/tắt chế độ gói quà riêng lẻ cho 1 sản phẩm.
     */
    @Transactional
    public void toggleItemGiftWrap(String cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart."));
        item.setIsGiftWrapped(!Boolean.TRUE.equals(item.getIsGiftWrapped()));
        cartItemRepository.save(item);
    }

    /**
     * Bật/tắt chế độ bọc bìa sách riêng lẻ cho 1 sản phẩm.
     * Kích hoạt {@link BookCoverDecorator} khi render lại trang giỏ hàng.
     */
    @Transactional
    public void toggleItemBookCover(String cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart."));
        item.setIsBookCovered(!Boolean.TRUE.equals(item.getIsBookCovered()));
        cartItemRepository.save(item);
    }

    /**
     * Bật/tắt chế độ chọn mua của 1 sản phẩm.
     */
    @Transactional
    public void toggleItemSelection(String cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ."));
        item.setIsSelected(!Boolean.TRUE.equals(item.getIsSelected()));
        cartItemRepository.save(item);
    }

    // =========================================================================
    //  DECORATOR PATTERN — Tính giá giỏ hàng
    // =========================================================================

    /**
     * Tính giá cuối cùng của giỏ hàng sử dụng <b>Decorator Pattern</b>.
     *
     * <p>Quá trình lắp ghép Decorator:</p>
     * <ol>
     *   <li>{@link BaseCartPricer} — Tính tổng giá cơ sở</li>
     *   <li>{@link GiftWrapDecorator} — Cộng phí gói quà (nếu bật)</li>
     *   <li>{@link VoucherDiscountDecorator} — Trừ giảm giá voucher (nếu có)</li>
     * </ol>
     *
     * @param cart Giỏ hàng cần tính giá
     * @return Tổng tiền cuối cùng
     */
    public BigDecimal calculateFinalPrice(Cart cart) {
        CartPricer pricer = buildDecoratorChain(cart);
        return pricer.calculatePrice();
    }

    /**
     * Tính chi tiết từng dòng giá để hiển thị trên UI (Price Breakdown).
     *
     * @param cart Giỏ hàng
     * @return Map chứa: subtotal, giftWrapFee, voucherDiscount, total
     */
    public Map<String, BigDecimal> calculatePriceBreakdown(Cart cart) {
        Map<String, BigDecimal> breakdown = new HashMap<>();

        // CHỈ TÍNH TOÁN CÁC SẢN PHẨM ĐƯỢC CHỌN (isSelected = true)
        java.util.List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::getIsSelected)
                .toList();

        // 1. Base price
        BaseCartPricer basePricer = new BaseCartPricer(selectedItems);
        BigDecimal subtotal = basePricer.calculatePrice();
        breakdown.put("subtotal", subtotal);

        // 2. Gift wrap fee
        BigDecimal giftWrapFee = BigDecimal.ZERO;
        long giftWrappedCount = selectedItems.stream()
                .filter(CartItem::getIsGiftWrapped)
                .count();

        if (giftWrappedCount > 0) {
            GiftWrapDecorator giftDecorator = new GiftWrapDecorator(basePricer, (int) giftWrappedCount);
            giftWrapFee = giftDecorator.getWrapFee();
        }
        breakdown.put("giftWrapFee", giftWrapFee);

        // 3. Book cover fee (BookCoverDecorator)
        BigDecimal bookCoverFee = BigDecimal.ZERO;
        long coveredBookCount = selectedItems.stream()
                .filter(CartItem::getIsBookCovered)
                .count();

        if (coveredBookCount > 0) {
            BookCoverDecorator coverDecorator = new BookCoverDecorator(basePricer, (int) coveredBookCount);
            bookCoverFee = coverDecorator.getCoverFee();
        }
        breakdown.put("bookCoverFee", bookCoverFee);

        // 4. Voucher discount
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (cart.getAppliedVoucher() != null && cart.getAppliedVoucher().isValid()) {
            // Voucher giảm trên (subtotal + giftWrap + bookCover)
            CartPricer priceBeforeVoucher = basePricer;
            if (giftWrappedCount > 0) {
                priceBeforeVoucher = new GiftWrapDecorator(priceBeforeVoucher, (int) giftWrappedCount);
            }
            if (coveredBookCount > 0) {
                priceBeforeVoucher = new BookCoverDecorator(priceBeforeVoucher, (int) coveredBookCount);
            }
            VoucherDiscountDecorator voucherDecorator =
                    new VoucherDiscountDecorator(priceBeforeVoucher, cart.getAppliedVoucher());
            voucherDiscount = voucherDecorator.getDiscountAmount();
        }
        breakdown.put("voucherDiscount", voucherDiscount);

        // 5. Total
        BigDecimal total = subtotal.add(giftWrapFee).add(bookCoverFee).subtract(voucherDiscount);
        breakdown.put("total", total.max(BigDecimal.ZERO));

        return breakdown;
    }

    /**
     * Xây dựng chuỗi Decorator (nội bộ).
     */
    private CartPricer buildDecoratorChain(Cart cart) {
        java.util.List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::getIsSelected)
                .toList();

        CartPricer pricer = new BaseCartPricer(selectedItems);

        long giftWrappedCount = selectedItems.stream()
                .filter(CartItem::getIsGiftWrapped)
                .count();

        if (giftWrappedCount > 0) {
            pricer = new GiftWrapDecorator(pricer, (int) giftWrappedCount);
        }

        long coveredBookCount = selectedItems.stream()
                .filter(CartItem::getIsBookCovered)
                .count();

        if (coveredBookCount > 0) {
            pricer = new BookCoverDecorator(pricer, (int) coveredBookCount);
        }

        if (cart.getAppliedVoucher() != null) {
            pricer = new VoucherDiscountDecorator(pricer, cart.getAppliedVoucher());
        }

        return pricer;
    }
}
