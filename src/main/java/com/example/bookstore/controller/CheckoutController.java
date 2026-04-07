package com.example.bookstore.controller;

import com.example.bookstore.dto.CheckoutDTO;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.PaymentMethodType;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.service.CartService;
import com.example.bookstore.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý luồng Checkout (thanh toán và đặt hàng).
 */
@Controller
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final CustomerRepository customerRepository;

    public CheckoutController(OrderService orderService,
                              CartService cartService,
                              CustomerRepository customerRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.customerRepository = customerRepository;
    }

    /**
     * Hiển thị trang Checkout.
     * <p>Trang này cho phép khách hàng xem lại giỏ hàng, nhập địa chỉ giao hàng
     * và chọn phương thức thanh toán (Strategy Pattern sẽ được áp dụng tại đây).</p>
     */
    @GetMapping("/checkout")
    public String checkoutPage(@ModelAttribute("currentUser") User currentUser,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Customer customer = findCustomer(currentUser);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/";
        }

        Cart cart = cartService.getOrCreateCart(customer);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/";
        }

        // Pre-fill địa chỉ từ Customer.address
        CheckoutDTO dto = new CheckoutDTO();
        dto.setAddress(customer.getAddress() != null ? customer.getAddress() : "");
        dto.setPaymentMethod(PaymentMethodType.COD);

        // Sử dụng Decorator Pattern để tính chi tiết giá (thread-safe: tính 1 lần, snapshot kết quả)
        java.util.Map<String, java.math.BigDecimal> priceBreakdown = cartService.calculatePriceBreakdown(cart);
        double shippingFee = 30000;

        model.addAttribute("checkoutDTO", dto);
        model.addAttribute("cart", cart);
        model.addAttribute("customer", customer);
        model.addAttribute("paymentMethods", PaymentMethodType.values());
        model.addAttribute("priceBreakdown", priceBreakdown);
        model.addAttribute("shippingFee", shippingFee);

        return "client/checkout";
    }

    /**
     * Xử lý đặt hàng khi khách bấm nút "Đặt hàng".
     * <p>Dữ liệu form (địa chỉ, phương thức thanh toán) được bind vào {@link CheckoutDTO}.
     * Sau đó gọi {@link OrderService#createOrderFromCart} để tạo đơn hàng,
     * nơi Strategy Pattern và Builder Pattern được thực thi.</p>
     */
    @PostMapping("/checkout")
    public String processCheckout(@ModelAttribute("currentUser") User currentUser,
                                  @ModelAttribute CheckoutDTO checkoutDTO,
                                  RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Customer customer = findCustomer(currentUser);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/";
        }

        try {
            Order order = orderService.createOrderFromCart(
                    customer,
                    checkoutDTO.getAddress(),
                    checkoutDTO.getPaymentMethod()
            );
            return "redirect:/order-success/" + order.getOrderId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    private Customer findCustomer(User user) {
        return customerRepository.findById(user.getId()).orElse(null);
    }
}
