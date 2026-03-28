package com.example.bookstore.controller;

import com.example.bookstore.dto.DashboardDTO;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final UserRepository userRepository;

    public AdminAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String adminLoginPage(@RequestParam(value = "error", required = false) String error,
                                 @RequestParam(value = "logout", required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        }
        if (logout != null) {
            model.addAttribute("success", "Đã đăng xuất thành công!");
        }
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        DashboardDTO dto = new DashboardDTO();

        // Header
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH));
        dto.setGreeting("Morning, Editor.");
        dto.setSubtitle("Here's the literary pulse for today, " + today + ".");
        dto.setSystemStatusLabel("Fully Synced");
        dto.setSystemStatusNote("Database online");

        // Current user
        String username = auth != null ? auth.getName() : "Admin";
        dto.setCurrentUserName(username);
        dto.setCurrentUserInitials(username.length() >= 2 ? username.substring(0, 2).toUpperCase() : "AD");
        dto.setCurrentUserRole("CHIEF EDITOR");
        dto.setCurrentUserEmail(username);

        // Metric cards — pull real counts from DB
        long customerCount = userRepository.count();

        dto.setRevenueCard(new DashboardDTO.MetricCard(
                "MONTHLY REVENUE", "$0.00", "—", true, "No orders yet"));
        dto.setOrdersCard(new DashboardDTO.MetricCard(
                "NEW ORDERS", "0", "—", true, "this month"));
        dto.setCustomersCard(new DashboardDTO.MetricCard(
                "ACTIVE READERS", String.valueOf(customerCount), "DB", true, "accounts stored in database"));
        dto.setStockCard(new DashboardDTO.MetricCard(
                "IN STOCK UNITS", "0", "Stable", true, "no titles yet"));

        // Revenue bars (placeholder)
        dto.setRevenueBars(Arrays.asList(
                new DashboardDTO.RevenueBar("Mon", "", 40, false),
                new DashboardDTO.RevenueBar("Tue", "", 55, false),
                new DashboardDTO.RevenueBar("Wed", "", 70, false),
                new DashboardDTO.RevenueBar("Thu", "", 90, true),
                new DashboardDTO.RevenueBar("Fri", "", 110, true),
                new DashboardDTO.RevenueBar("Sat", "", 60, false),
                new DashboardDTO.RevenueBar("Sun", "", 45, false)
        ));
        dto.setDataSourceNote("data.json + database online");

        // Activity
        dto.setActivities(Arrays.asList(
                new DashboardDTO.ActivityItem("G", "Google Sign-In",
                        "OAuth2 is active. Successful logins are saved to database.", "Active", "positive"),
                new DashboardDTO.ActivityItem("L", "Local Registration",
                        "Form-based signup with BCrypt password hash.", "Active", "positive"),
                new DashboardDTO.ActivityItem("D", "Database Sync",
                        "Users table synced with Aiven MySQL cloud.", "Online", "positive")
        ));

        // Inventory
        dto.setInventorySearchPlaceholder("Search Inventory...");
        dto.setBooks(Collections.emptyList());

        model.addAttribute("dashboard", dto);
        return "admin/dashboard";
    }
}
