package com.example.bookstore.service;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.Payment;
import com.example.bookstore.entity.enums.PaymentMethodType;
import com.example.bookstore.entity.enums.PaymentStatus;
import com.example.bookstore.repository.PaymentRepository;
import com.example.bookstore.service.payment.PaymentStrategy;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * <b>Strategy Pattern — Context</b>
 *
 * <p>PaymentService đóng vai trò là <b>Context</b> trong Strategy Pattern.
 * Class này duy trì một tham chiếu đến {@link PaymentStrategy} thông qua
 * {@code Map<PaymentMethodType, PaymentStrategy>}, và ủy thác
 * (delegate) việc xử lý thanh toán cho Strategy tương ứng.</p>
 *
 * <p><b>Cách hoạt động:</b></p>
 * <ol>
 *   <li>Tại thời điểm khởi tạo, Context nạp tất cả Concrete Strategy vào Map
 *       (key = Enum, value = Strategy instance).</li>
 *   <li>Khi client gọi {@link #processPayment}, Context tra cứu (lookup) đúng
 *       Strategy dựa trên {@link PaymentMethodType} và gọi
 *       {@link PaymentStrategy#processPayment(double)}.</li>
 *   <li>Context KHÔNG biết chi tiết bên trong của mỗi Strategy
 *       → tuân thủ nguyên lý <b>Dependency Inversion</b>.</li>
 * </ol>
 *
 //* @see PaymentStrategy
 //* @see CODStrategy
 //* @see BankTransferStrategy
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    /**
     * Map lưu trữ các Strategy: key là Enum phương thức, value là Strategy tương ứng.
     * Đây là cấu trúc cốt lõi của Strategy Pattern — cho phép chọn thuật toán tại runtime.
     */
    private final Map<PaymentMethodType, PaymentStrategy> strategyMap;

    private final PaymentRepository paymentRepository;

    /**
     * Constructor — Spring tự động tiêm toàn bộ bean implement {@link PaymentStrategy}.
     * <p>Sử dụng Ín tượng <b>Auto-Discovery</b>: khi thêm Strategy mới, chỉ cần
     * đánh dấu nó với {@code @Component} — Context không cần sửa.</p>
     * <p>Tuân thủ  100% <b>Open/Closed Principle</b>: mở để mở rộng, đóng để sửa đổi.</p>
     */
    public PaymentService(List<PaymentStrategy> strategies,
                          PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;

        // Auto-Discovery: nạp từng Strategy vào EnumMap bằng vòng lặp for
        this.strategyMap = new EnumMap<>(PaymentMethodType.class);
        for (PaymentStrategy strategy : strategies) {
            this.strategyMap.put(strategy.getSupportedMethod(), strategy);
        }
    }

    /**
     * Xử lý thanh toán cho một đơn hàng bằng phương thức được chỉ định.
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Tra cứu Strategy từ {@code strategyMap} dựa trên {@code method}.</li>
     *   <li>Tạo đối tượng {@link Payment} với trạng thái ban đầu là {@code UNPAID}.</li>
     *   <li>Gọi {@link PaymentStrategy#processPayment(double)} — ủy thác cho Strategy.</li>
     *   <li>Lưu Payment vào database.</li>
     * </ol>
     *
     * @param order  Đơn hàng cần thanh toán
     * @param method Phương thức thanh toán do khách hàng chọn
     * @return Đối tượng {@link Payment} đã được lưu vào database
     * @throws IllegalArgumentException nếu phương thức thanh toán không được hỗ trợ
     */
    @Transactional
    public Payment processPayment(Order order, PaymentMethodType method) {
        // 1. Tra cứu Strategy — đây là điểm mấu chốt của Strategy Pattern
        PaymentStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ: " + method);
        }

        // 2. Tạo đối tượng Payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.UNPAID);

        // 3. Ủy thác (delegate) xử lý cho Concrete Strategy tương ứng
        logger.info("=== BẮT ĐẦU XỬ LÝ THANH TOÁN ===");
        logger.info("Phương thức: {} | Số tiền: {}", method, order.getTotalAmount());

        boolean success = strategy.processPayment(order.getTotalAmount().doubleValue());

        if (success) {
            logger.info("=== KẾT QUẢ: THANH TOÁN THÀNH CÔNG ===");
        } else {
            logger.warn("=== KẾT QUẢ: THANH TOÁN THẤT BẠI ===");
        }

        // 4. Lưu Payment vào database
        return paymentRepository.save(payment);
    }
}
