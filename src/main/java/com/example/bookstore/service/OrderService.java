package com.example.bookstore.service;

import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;
import com.example.bookstore.entity.enums.PaymentMethodType;
import com.example.bookstore.pattern.observer.OrderObserver;
import com.example.bookstore.pattern.observer.OrderStatusSubject;
import com.example.bookstore.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service điều phối (orchestrate) quá trình tạo và quản lý đơn hàng.
 *
 * <p>
 * Class này kết hợp <b>Builder Pattern</b> (để xây dựng đối tượng Order)
 * và <b>Strategy Pattern</b> (thông qua {@link PaymentService} để xử lý thanh
 * toán).
 * Đây là điểm giao tiếp giữa 2 Design Pattern trong hệ thống.
 * </p>
 *
 * <p>
 * <b>Observer Pattern (OrderStatusSubject):</b> Class này implements
 * {@link OrderStatusSubject}.
 * Spring Boot tự động inject toàn bộ bean {@code @Component} có implements
 * {@link OrderObserver}
 * vào {@code List<OrderObserver>} thông qua Constructor Injection.
 * Nguyên tắc OCP được tuân thủ: thêm Observer mới chỉ cần tạo file mới với
 * {@code @Component},
 * không cần sửa file này.
 * </p>
 */
@Service
public class OrderService implements OrderStatusSubject {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /** Phí vận chuyển mặc định (có thể cấu hình sau). */
    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final CartService cartService;

    /**
     * Spring tự động gom tất cả @Component implements OrderObserver vào list này.
     * Tuân thủ Open/Closed Principle: thêm Observer mới không cần sửa Constructor.
     */
    private final List<OrderObserver> orderObservers;

    public OrderService(OrderRepository orderRepository,
            PaymentService paymentService,
            CartService cartService,
            List<OrderObserver> orderObservers) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.cartService = cartService;
        // Wrap bằng ArrayList để đảm bảo list có thể modify (add/remove) được.
        // Spring inject List<?> đôi khi là unmodifiableList — cần wrap để an toàn.
        this.orderObservers = new ArrayList<>(orderObservers);
    }

    // ======================== OBSERVER PATTERN ========================

    @Override
    public void addOrderObserver(OrderObserver observer) {
        orderObservers.add(observer);
    }

    @Override
    public void removeOrderObserver(OrderObserver observer) {
        orderObservers.remove(observer);
    }

    @Override
    public void notifyOrderObservers(Order order, OrderStatus newStatus) {
        orderObservers.forEach(observer -> observer.update(order, newStatus));
    }

    /**
     * Tạo đơn hàng hoàn chỉnh từ giỏ hàng của khách.
     *
     * <p>
     * <b>Luồng xử lý:</b>
     * </p>
     * <ol>
     * <li>Sử dụng {@link Order.Builder} (Builder Pattern) để xây dựng đối tượng
     * Order
     * từ dữ liệu Cart, địa chỉ, phí ship.</li>
     * <li>Lưu Order vào database (cascade sẽ tự lưu OrderItem + Shipping).</li>
     * <li>Gọi {@link PaymentService#processPayment} (Strategy Pattern) để xử lý
     * thanh toán
     * theo phương thức đã chọn.</li>
     * <li>Xóa sạch giỏ hàng sau khi đặt hàng thành công.</li>
     * </ol>
     *
     * @param customer      Khách hàng đặt đơn
     * @param address       Địa chỉ giao hàng
     * @param paymentMethod Phương thức thanh toán (COD / BANK_TRANSFER)
     * @return Đơn hàng đã được tạo và lưu vào database
     * @throws IllegalStateException nếu giỏ hàng trống
     */
    @Transactional
    public Order createOrderFromCart(Customer customer, String address,
            PaymentMethodType paymentMethod) {
        // 1. Lấy giỏ hàng
        Cart cart = cartService.getOrCreateCart(customer);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể đặt hàng.");
        }

        logger.info("Bắt đầu tạo đơn hàng cho khách: {} | Phương thức: {}",
                customer.getFullName(), paymentMethod);

        // 2. THREAD-SAFE: Tính giá 1 lần bằng Decorator Pattern, snapshot kết quả
        //    Tránh vấn đề đa luồng — không gọi Decorator chain nhiều lần
        java.util.Map<String, java.math.BigDecimal> priceBreakdown =
                cartService.calculatePriceBreakdown(cart);
        java.math.BigDecimal decoratorTotal = priceBreakdown.get("total"); // subtotal + giftWrap - voucher

        // 3. Sử dụng BUILDER PATTERN để xây dựng Order hoàn chỉnh
        java.util.Set<com.example.bookstore.entity.CartItem> selectedItems = cart.getItems().stream()
                .filter(com.example.bookstore.entity.CartItem::getIsSelected)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        if (selectedItems.isEmpty()) {
            throw new IllegalStateException("Bạn chưa chọn sản phẩm nào để thanh toán.");
        }

        Order.Builder builder = Order.builder()
                .customer(customer)
                .address(address)
                .cartItems(selectedItems)
                .shippingFee(DEFAULT_SHIPPING_FEE)
                .appliedVoucher(cart.getAppliedVoucher())
                .paymentMethod(paymentMethod);

        Order order = builder.build();

        // 4. Ghi đè totalAmount bằng giá từ Decorator (bao gồm gift wrap + voucher discount)
        //    thay vì chỉ subtotal + shipping từ Builder
        order.setTotalAmount(decoratorTotal.add(DEFAULT_SHIPPING_FEE));

        // 5. Lưu Order vào database (cascade lưu OrderItem + Shipping)
        order = orderRepository.save(order);
        logger.info("Đơn hàng đã được tạo: {} | Tổng tiền: {}",
                order.getOrderId(), order.getTotalAmount());

        // 6. Sử dụng STRATEGY PATTERN để xử lý thanh toán
        paymentService.processPayment(order, paymentMethod);

        // 7. Tăng usedCount của Voucher (nếu có) — thread-safe vì trong @Transactional
        if (cart.getAppliedVoucher() != null && cart.getAppliedVoucher().isValid()) {
            cart.getAppliedVoucher().setUsedCount(
                    cart.getAppliedVoucher().getUsedCount() + 1);
        }

        // 8. Xóa sạch giỏ hàng sau khi đặt hàng thành công
        cartService.clearCart(cart);
        logger.info("Đã xóa giỏ hàng của khách: {}", customer.getFullName());

        return order;
    }

    /**
     * Lấy danh sách đơn hàng của một khách hàng, sắp xếp theo ngày mới nhất.
     *
     * @param customerId ID của khách hàng
     * @return Danh sách đơn hàng
     */
    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomer_IdWithDetails(customerId);
    }

    /**
     * Lấy chi tiết một đơn hàng theo ID.
     *
     * @param orderId ID của đơn hàng
     * @return Optional chứa đơn hàng nếu tìm thấy
     */
    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findByIdWithItems(orderId);
    }

    // ======================== ADMIN: QUAN LY DON HANG ========================

    /**
     * Lấy toàn bộ đơn hàng trong hệ thống, sắp xếp theo ngày mới nhất.
     * Dành cho Admin Order Management.
     *
     * @return Danh sách tất cả đơn hàng
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithItemsOrderByOrderDateDesc();
    }

    /**
     * Admin chuyển trạng thái đơn hàng và kích hoạt toàn bộ OrderObserver.
     *
     * <p>
     * <b>Luồng xử lý:</b>
     * </p>
     * <ol>
     * <li>Tìm đơn hàng theo ID (ném exception nếu không tìm thấy).</li>
     * <li>Cập nhật trạng thái mới và lưu vào database.</li>
     * <li>Gọi {@link #notifyOrderObservers} → kích hoạt Email + SMS Observer.</li>
     * </ol>
     *
     * @param orderId   ID đơn hàng cần cập nhật
     * @param newStatus Trạng thái mới
     * @return Đơn hàng đã được cập nhật
     * @throws IllegalArgumentException nếu không tìm thấy đơn hàng
     */
    @Transactional
    public Order updateStatus(String orderId, OrderStatus newStatus) {
        // Dùng findByIdWithItems để load eager items + books
        // → StockAdjustmentObserver có thể duyệt order.getItems() mà không bị LazyInitializationException
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn hàng với ID: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) {
            logger.warn("updateStatus bị gọi nhưng status không đổi ({}), bỏ qua.", newStatus);
            return order;
        }

        // ===================================
        // SỬ DỤNG STATE PATTERN THAY VÌ GÁN ĐÈ
        // ===================================
        if (newStatus == OrderStatus.CANCELLED) {
            order.cancelOrder();
        } else {
            // Cố gắng chuyển sang trạng thái kế tiếp trong luồng
            order.nextState();
            
            // Validate: Nếu trạng thái sau khi next không khớp với newStatus (admin truyền vào sai tuần tự)
            if (order.getStatus() != newStatus) {
                throw new IllegalArgumentException(
                        "Chuyển trạng thái không hợp lệ. Không thể chuyển từ " + oldStatus + " trực tiếp sang " + newStatus);
            }
        }

        order = orderRepository.save(order);
        logger.info("Admin cập nhật đơn #{}: {} → {}", orderId, oldStatus, newStatus);

        // Observer Pattern — kích hoạt toàn bộ OrderObserver đã đăng ký:
        // EmailNotifierObserver, SMSNotifierObserver, OrderNotificationObserver,
        // StockAdjustmentObserver (trừ/cộng kho)
        notifyOrderObservers(order, newStatus);
        return order;
    }
}
