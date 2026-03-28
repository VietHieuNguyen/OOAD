package com.example.bookstore.entity;

import com.example.bookstore.entity.enums.OrderStatus;
import com.example.bookstore.entity.enums.PaymentMethodType;
import com.example.bookstore.entity.enums.ShippingStatus;
import com.example.bookstore.util.IdGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity đại diện cho một đơn hàng trong hệ thống BookStore.
 *
 * <p>Class này chứa một <b>Builder Pattern</b> dưới dạng static inner class
 * {@link Builder} để khởi tạo đối tượng Order hoàn chỉnh từ dữ liệu giỏ hàng.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id", length = 36, nullable = false, updatable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 255)
    private String address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Shipping> shippings = new LinkedHashSet<>();

    @PrePersist
    protected void prePersist() {
        if (orderId == null || orderId.isBlank()) {
            orderId = IdGenerator.newId();
        }
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Tính tổng tiền đơn hàng từ danh sách các OrderItem.
     * <p>Công thức: {@code totalAmount = Σ(orderItem.getSubTotal())}</p>
     *
     * @return Tổng tiền đơn hàng
     */
    public BigDecimal calculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return this.totalAmount;
    }

    /**
     * Tạo một Builder mới để khởi tạo đối tượng Order.
     *
     * @return instance của {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    // =========================================================================
    //  BUILDER PATTERN — Static Inner Class
    // =========================================================================

    /**
     * <b>Builder Pattern — Xây dựng đối tượng Order hoàn chỉnh</b>
     *
     * <p><b>Tại sao áp dụng Builder Pattern ở đây?</b></p>
     * <ul>
     *   <li>Đối tượng {@link Order} cần được khởi tạo từ nhiều nguồn dữ liệu khác nhau:
     *       {@link Cart} (danh sách CartItem), {@link Customer}, địa chỉ giao hàng,
     *       phí vận chuyển, phương thức thanh toán.</li>
     *   <li>Builder Pattern giúp tách biệt quá trình xây dựng (construction) ra khỏi
     *       biểu diễn (representation), giúp code dễ đọc và giảm thiểu constructor
     *       với quá nhiều tham số (<b>Telescoping Constructor Anti-Pattern</b>).</li>
     *   <li>Đảm bảo đối tượng Order luôn ở trạng thái hợp lệ sau khi build
     *       (đã có items, đã tính tổng tiền, đã có shipping).</li>
     * </ul>
     *
     * <p><b>Ví dụ sử dụng:</b></p>
     * <pre>{@code
     * Order order = Order.builder()
     *     .customer(customer)
     *     .address("123 Nguyễn Huệ, Q1, TP.HCM")
     *     .cartItems(cart.getItems())
     *     .shippingFee(new BigDecimal("30000"))
     *     .paymentMethod(PaymentMethodType.COD)
     *     .build();
     * }</pre>
     *
     * @see Order
     */
    public static class Builder {

        private Customer customer;
        private String address;
        private Set<CartItem> cartItems = new LinkedHashSet<>();
        private BigDecimal shippingFee = BigDecimal.ZERO;
        private PaymentMethodType paymentMethod;

        /** Thiết lập khách hàng đặt đơn. */
        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        /** Thiết lập địa chỉ giao hàng. */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /** Thiết lập danh sách sản phẩm từ giỏ hàng. */
        public Builder cartItems(Set<CartItem> cartItems) {
            this.cartItems = cartItems;
            return this;
        }

        /** Thiết lập phí vận chuyển. */
        public Builder shippingFee(BigDecimal shippingFee) {
            this.shippingFee = shippingFee;
            return this;
        }

        /** Thiết lập phương thức thanh toán. */
        public Builder paymentMethod(PaymentMethodType paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        /** Trả về phương thức thanh toán đã chọn (dùng cho Service layer). */
        public PaymentMethodType getPaymentMethod() {
            return this.paymentMethod;
        }

        /**
         * Xây dựng đối tượng {@link Order} hoàn chỉnh.
         *
         * <p><b>Các bước xử lý trong build():</b></p>
         * <ol>
         *   <li>Validate dữ liệu đầu vào (customer, address, cartItems).</li>
         *   <li>Tạo đối tượng Order với thông tin cơ bản.</li>
         *   <li>Chuyển đổi từng {@link CartItem} thành {@link OrderItem}
         *       (snapshot giá tại thời điểm mua — {@code priceAtPurchase}).</li>
         *   <li>Tạo bản ghi {@link Shipping} mặc định với phí ship đã chỉ định.</li>
         *   <li>Gọi {@link Order#calculateTotal()} để tính tổng tiền đơn hàng.</li>
         * </ol>
         *
         * @return Đối tượng {@link Order} đã sẵn sàng để lưu vào database
         * @throws IllegalStateException nếu thiếu dữ liệu bắt buộc
         */
        public Order build() {
            // --- Validation ---
            if (customer == null) {
                throw new IllegalStateException("Customer là bắt buộc để tạo đơn hàng.");
            }
            if (address == null || address.isBlank()) {
                throw new IllegalStateException("Địa chỉ giao hàng là bắt buộc.");
            }
            if (cartItems == null || cartItems.isEmpty()) {
                throw new IllegalStateException("Giỏ hàng trống, không thể tạo đơn hàng.");
            }

            // --- Khởi tạo Order ---
            Order order = new Order();
            order.setCustomer(customer);
            order.setAddress(address);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);

            // --- Chuyển đổi CartItem → OrderItem (Snapshot giá) ---
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setBook(cartItem.getBook());
                orderItem.setQuantity(cartItem.getQuantity());
                // Snapshot: lưu giá tại thời điểm mua, không tham chiếu giá hiện tại
                orderItem.setPriceAtPurchase(cartItem.getUnitPrice());
                order.getItems().add(orderItem);
            }

            // --- Tạo Shipping record mặc định ---
            Shipping shipping = new Shipping();
            shipping.setOrder(order);
            shipping.setShippingFee(shippingFee);
            shipping.setStatus(ShippingStatus.PREPARING);
            order.getShippings().add(shipping);

            // --- Tính tổng tiền (bao gồm phí ship) ---
            order.calculateTotal();
            order.setTotalAmount(order.getTotalAmount().add(shippingFee));

            return order;
        }
    }
}
