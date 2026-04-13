package com.example.bookstore.controller;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;
import com.example.bookstore.service.OrderService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý đơn hàng dành cho Admin.
 *
 * <p>
 * <b>Luồng hoạt động chính (MVC):</b>
 * </p>
 * <ol>
 * <li>Admin truy cập {@code /admin/orders} → hiển thị danh sách tất cả đơn
 * hàng.</li>
 * <li>Click vào một đơn → {@code /admin/orders/{id}} → xem chi tiết + form
 * chuyển trạng thái.</li>
 * <li>Submit form → {@code POST /admin/orders/{id}/update-status} →
 * gọi {@link OrderService#updateStatus} → kích hoạt Observer (Email + SMS) →
 * redirect về detail.</li>
 * </ol>
 *
 * <p>
 * <b>Lưu ý Observer Pattern:</b> Việc chuyển trạng thái ở đây sẽ tự động
 * kích hoạt toàn bộ {@code OrderObserver} đã đăng ký trong
 * {@code OrderService}.
 * Controller này không cần biết Observer nào đang lắng nghe.
 * </p>
 */
@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private static final int PAGE_SIZE = 5;

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ======================== LIST ========================

    /**
     * Hiển thị danh sách đơn hàng với filter (keyword, status, sort) và phân trang.
     */
    @GetMapping
    public String listOrders(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "status", defaultValue = "") String status,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        List<Order> all = orderService.getAllOrders();

        // --- Filter by keyword (order ID prefix or book title) ---
        if (!keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            all = all.stream()
                    .filter(o -> o.getOrderId().toLowerCase().contains(kw)
                            || (o.getCustomer() != null && o.getCustomer().getFullName() != null && o.getCustomer().getFullName().toLowerCase().contains(kw))
                            || o.getItems().stream()
                                    .anyMatch(item -> item.getBook().getTitle().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        // --- Filter by status ---
        if (!status.isBlank()) {
            try {
                OrderStatus st = OrderStatus.valueOf(status);
                all = all.stream()
                        .filter(o -> o.getStatus() == st)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {
                /* invalid status → ignore */ }
        }

        // --- Sort ---
        if ("oldest".equals(sort)) {
            all.sort(Comparator.comparing(Order::getOrderDate));
        }
        // "newest" already ordered by repository

        // --- Paginate ---
        int totalOrders = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalOrders / PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));
        int fromIdx = (page - 1) * PAGE_SIZE;
        int toIdx = Math.min(fromIdx + PAGE_SIZE, totalOrders);
        List<Order> paged = all.subList(fromIdx, toIdx);

        model.addAttribute("orders", paged);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("pageTitle", "Manage Orders");
        return "admin/orders";
    }

    // ======================== DETAIL ========================

    /**
     * Hiển thị chi tiết một đơn hàng + nút action theo trạng thái.
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") String id,
            Model model,
            RedirectAttributes redirectAttributes) {
        return orderService.getOrderById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("pageTitle", "Order Detail");
                    return "admin/order-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Order not found with ID: " + id);
                    return "redirect:/admin/orders";
                });
    }

    // ======================== UPDATE STATUS ========================

    /**
     * Admin chuyển trạng thái đơn hàng (quick action từ list hoặc form từ detail).
     *
     * <p>
     * Sau khi cập nhật thành công, {@link OrderService#updateStatus} sẽ tự động
     * kích hoạt tất cả {@code OrderObserver} (EmailNotifier, SMSNotifier,...).
     * Controller này chỉ cần redirect về trang chi tiết với flash message.
     * </p>
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable("id") String id,
            @RequestParam("newStatus") OrderStatus newStatus,
            @RequestParam(value = "redirectTo", defaultValue = "detail") String redirectTo,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order status updated to: " + newStatus.name());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating status: " + e.getMessage());
        }

        if ("list".equals(redirectTo)) {
            return "redirect:/admin/orders";
        }
        return "redirect:/admin/orders/" + id;
    }
}
