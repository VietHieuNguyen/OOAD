package com.example.bookstore.service;

import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.PaymentMethodType;
import com.example.bookstore.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service điều phối (orchestrate) quá trình tạo đơn hàng.
 *
 * <p>Class này kết hợp <b>Builder Pattern</b> (để xây dựng đối tượng Order)
 * và <b>Strategy Pattern</b> (thông qua {@link PaymentService} để xử lý thanh toán).
 * Đây là điểm giao tiếp giữa 2 Design Pattern trong hệ thống.</p>
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /** Phí vận chuyển mặc định (có thể cấu hình sau). */
    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        PaymentService paymentService,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.cartService = cartService;
    }

    /**
     * Tạo đơn hàng hoàn chỉnh từ giỏ hàng của khách.
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Sử dụng {@link Order.Builder} (Builder Pattern) để xây dựng đối tượng Order
     *       từ dữ liệu Cart, địa chỉ, phí ship.</li>
     *   <li>Lưu Order vào database (cascade sẽ tự lưu OrderItem + Shipping).</li>
     *   <li>Gọi {@link PaymentService#processPayment} (Strategy Pattern) để xử lý thanh toán
     *       theo phương thức đã chọn.</li>
     *   <li>Xóa sạch giỏ hàng sau khi đặt hàng thành công.</li>
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

        // 2. Sử dụng BUILDER PATTERN để xây dựng Order hoàn chỉnh
        Order.Builder builder = Order.builder()
                .customer(customer)
                .address(address)
                .cartItems(cart.getItems())
                .shippingFee(DEFAULT_SHIPPING_FEE)
                .paymentMethod(paymentMethod);

        Order order = builder.build();

        // 3. Lưu Order vào database (cascade lưu OrderItem + Shipping)
        order = orderRepository.save(order);
        logger.info("Đơn hàng đã được tạo: {} | Tổng tiền: {}",
                order.getOrderId(), order.getTotalAmount());

        // 4. Sử dụng STRATEGY PATTERN để xử lý thanh toán
        paymentService.processPayment(order, paymentMethod);

        // 5. Xóa sạch giỏ hàng sau khi đặt hàng thành công
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
        return orderRepository.findByCustomer_Id(customerId);
    }

    /**
     * Lấy chi tiết một đơn hàng theo ID.
     *
     * @param orderId ID của đơn hàng
     * @return Optional chứa đơn hàng nếu tìm thấy
     */
    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }
}
