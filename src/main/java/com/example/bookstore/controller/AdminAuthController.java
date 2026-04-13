package com.example.bookstore.controller;

import com.example.bookstore.dto.DashboardDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;

    public AdminAuthController(UserRepository userRepository,
                               BookRepository bookRepository,
                               OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/login")
    public String adminLoginPage(@RequestParam(value = "error", required = false) String error,
                                 @RequestParam(value = "logout", required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("success", "Logged out successfully!");
        }
        return "admin/login";
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model, Authentication auth) {
        DashboardDTO dto = new DashboardDTO();

        // Header
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH));
        dto.setGreeting("Dashboard Overview");
        dto.setSubtitle("Store activity overview \u2014 " + today + ".");
        dto.setSystemStatusLabel("Fully Synced");
        dto.setSystemStatusNote("Database online");

        // Current user
        String username = auth != null ? auth.getName() : "Admin";
        dto.setCurrentUserName(username);
        dto.setCurrentUserInitials(username.length() >= 2 ? username.substring(0, 2).toUpperCase() : "AD");
        dto.setCurrentUserRole("CHIEF EDITOR");
        dto.setCurrentUserEmail(username);

        // ==================== REAL DATA FROM DB ====================

        // Book stats
        long totalBooks = bookRepository.count();
        long activeBooks = bookRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .count();
        long totalStock = bookRepository.findAll().stream()
                .mapToLong(b -> b.getStockQuantity() != null ? b.getStockQuantity() : 0)
                .sum();

        // Order stats
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();

        // Revenue: tính tổng từ đơn COMPLETED
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Orders this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long ordersThisMonth = allOrders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().isAfter(startOfMonth))
                .count();

        // Pending orders count
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();
        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long cancelledOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();

        model.addAttribute("pieChartLabels", "['Pending', 'Completed', 'Cancelled']");
        model.addAttribute("pieChartData", "[" + pendingOrders + "," + completedOrders + "," + cancelledOrders + "]");

        long customerCount = userRepository.count();

        // ==================== METRIC CARDS ====================

        // Format revenue
        String revenueStr;
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            revenueStr = "0 ₫";
        } else if (totalRevenue.compareTo(new BigDecimal("1000000")) >= 0) {
            revenueStr = String.format("%,.0f ₫", totalRevenue);
        } else {
            revenueStr = String.format("%,.0f ₫", totalRevenue);
        }

        dto.setRevenueCard(new DashboardDTO.MetricCard(
                "TOTAL REVENUE",
                revenueStr,
                "COMPLETED",
                true,
                totalOrders + " total orders"));

        dto.setOrdersCard(new DashboardDTO.MetricCard(
                "ORDERS THIS MONTH",
                String.valueOf(ordersThisMonth),
                pendingOrders > 0 ? pendingOrders + " pending" : "All processed",
                pendingOrders == 0,
                "Total: " + totalOrders + " orders"));

        dto.setCustomersCard(new DashboardDTO.MetricCard(
                "USERS",
                String.valueOf(customerCount),
                "DB",
                true,
                "accounts in system"));

        dto.setStockCard(new DashboardDTO.MetricCard(
                "TOTAL BOOKS",
                String.valueOf(totalBooks),
                activeBooks + " active",
                true,
                "Total stock: " + totalStock + " units"));

        // ==================== LINE CHART DATA (7 ngày gần nhất) ====================
        // Build daily order counts and revenue for the last 7 days
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartOrderCounts = new ArrayList<>();
        List<String> chartOrderCountsJson = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

            long count = allOrders.stream()
                    .filter(o -> o.getOrderDate() != null
                            && !o.getOrderDate().isBefore(dayStart)
                            && o.getOrderDate().isBefore(dayEnd))
                    .count();

            chartLabels.add("\"" + day.format(dayFmt) + "\"");
            chartOrderCountsJson.add(String.valueOf(count));
        }

        model.addAttribute("chartLabels", "[" + String.join(",", chartLabels) + "]");
        model.addAttribute("chartOrderCounts", "[" + String.join(",", chartOrderCountsJson) + "]");

        // ==================== INVENTORY TABLE ====================
        List<Book> recentBooks = bookRepository.findAll().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
                .collect(Collectors.toList());
        List<DashboardDTO.BookRow> bookRows = recentBooks.stream()
                .limit(10)
                .map(b -> {
                    DashboardDTO.BookRow row = new DashboardDTO.BookRow();
                    row.setTitle(b.getTitle());
                    row.setAuthor(b.getAuthor());
                    row.setThumbnail(b.getThumbnail());
                    row.setCategory(b.getCategory() != null ? b.getCategory().getName() : "N/A");
                    int stock = b.getStockQuantity() != null ? b.getStockQuantity() : 0;
                    boolean active = Boolean.TRUE.equals(b.getIsActive());
                    if (!active) {
                        row.setStatusLabel("Inactive");
                        row.setStatusClass("pre-order");
                    } else if (stock > 10) {
                        row.setStatusLabel("In Stock");
                        row.setStatusClass("in-stock");
                    } else if (stock > 0) {
                        row.setStatusLabel("Low Stock");
                        row.setStatusClass("low-stock");
                    } else {
                        row.setStatusLabel("Out of Stock");
                        row.setStatusClass("out");
                    }
                    row.setStockLabel(stock + " units");
                    return row;
                })
                .collect(Collectors.toList());

        dto.setInventorySearchPlaceholder("Search books...");
        dto.setBooks(bookRows);
        dto.setDataSourceNote("Real-time data from database");

        // Activities still hardcoded but relevant
        dto.setActivities(Arrays.asList(
                new DashboardDTO.ActivityItem("DB", "Database",
                        "Database connection stable. All data synchronized.", "Online", "positive"),
                new DashboardDTO.ActivityItem("G", "Google OAuth2",
                        "Google login active. Accounts are securely stored.", "Active", "positive"),
                new DashboardDTO.ActivityItem("📊", "Dashboard",
                        totalBooks + " books, " + totalOrders + " orders, " + customerCount + " users.", "Live", "positive")
        ));

        model.addAttribute("dashboard", dto);
        return "admin/dashboard";
    }
}
