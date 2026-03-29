package com.example.bookstore.controller;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.service.OrderService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller quản lý hiển thị đơn hàng phía khách hàng.
 */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    public OrderController(OrderService orderService,
                           CustomerRepository customerRepository) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
    }

    /**
     * Trang đặt hàng thành công — hiển thị sau khi checkout hoàn tất.
     */
    @GetMapping("/order-success/{orderId}")
    public String orderSuccessPage(@PathVariable String orderId,
                                   @ModelAttribute("currentUser") User currentUser,
                                   Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        model.addAttribute("order", orderOpt.get());
        return "client/order-success";
    }

    /**
     * Danh sách đơn hàng của khách hàng đang đăng nhập.
     */
    @GetMapping("/orders")
    public String ordersPage(@ModelAttribute("currentUser") User currentUser,
                             Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Customer customer = customerRepository.findById(currentUser.getId()).orElse(null);
        if (customer == null) {
            return "redirect:/";
        }

        List<Order> orders = orderService.getOrdersByCustomer(customer.getId());
        model.addAttribute("orders", orders);
        return "client/orders";
    }

    /**
     * Chi tiết một đơn hàng — hiển thị thông tin sản phẩm, thanh toán, vận chuyển.
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetailPage(@PathVariable String orderId,
                                  @ModelAttribute("currentUser") User currentUser,
                                  Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOpt.get();

        // Bảo mật: chỉ cho phép xem đơn hàng của chính mình
        if (!order.getCustomer().getId().equals(currentUser.getId())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "client/order-detail";
    }
}
