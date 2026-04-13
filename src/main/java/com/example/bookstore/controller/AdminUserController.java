package com.example.bookstore.controller;

import com.example.bookstore.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Customer dành cho Admin.
 * <p>Cho phép Admin xem danh sách khách hàng và bật/tắt trạng thái active.</p>
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị danh sách tất cả Customer.
     */
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("customers", userService.findAllCustomers());
        model.addAttribute("pageTitle", "Manage Customers");
        return "admin/users";
    }

    /**
     * Bật/tắt trạng thái active của một user.
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") String id,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.toggleActive(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
