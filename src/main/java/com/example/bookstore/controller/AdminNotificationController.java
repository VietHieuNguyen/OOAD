package com.example.bookstore.controller;

import com.example.bookstore.entity.Notification;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.NotificationRepository;
import com.example.bookstore.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller cho giao diện chuông thông báo (Notification Bell) và trang thông báo Admin.
 *
 * <p>Kết hợp 2 vai trò:
 * <ul>
 *   <li>REST API ({@code /admin/api/notifications/**}) — được gọi qua Fetch API từ JS
 *       để hiển thị popover chuông.</li>
 *   <li>MVC page ({@code /admin/notifications}) — trang xem tất cả thông báo, có phân trang.</li>
 * </ul>
 * </p>
 *
 * <p>Thiết kế tách biệt với AdminOrderController để đảm bảo SRP.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminNotificationController {

    private static final int API_PREVIEW_SIZE = 10;
    private static final int PAGE_SIZE        = 15;

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;

    public AdminNotificationController(NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository         = userRepository;
    }

    // ======================== REST API (cho Popover chuông) ========================

    /**
     * Lấy tối đa 10 thông báo mới nhất của user đang đăng nhập.
     */
    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.status(401).build();

        List<Notification> all = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        List<Notification> preview = all.size() > API_PREVIEW_SIZE
                ? all.subList(0, API_PREVIEW_SIZE) : all;

        long unreadCount = notificationRepository.countByUser_IdAndIsReadFalse(user.getId());

        return ResponseEntity.ok(Map.of(
                "notifications", preview.stream().map(this::toDto).toList(),
                "unreadCount",   unreadCount,
                "totalCount",    (long) all.size()
        ));
    }

    /**
     * Đánh dấu 1 thông báo đã đọc.
     */
    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@PathVariable String id, Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.status(401).build();

        return notificationRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .map(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Đánh dấu tất cả thông báo chưa đọc của user hiện tại là đã đọc.
     */
    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllRead(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.status(401).build();

        List<Notification> unread = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);

        return ResponseEntity.ok(Map.of("marked", unread.size()));
    }

    // ======================== MVC PAGE (trang xem tất cả) ========================

    /**
     * Trang /admin/notifications — hiển thị toàn bộ thông báo với phân trang.
     */
    @GetMapping("/notifications")
    public String notificationsPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Authentication authentication,
            Model model) {

        User user = resolveUser(authentication);
        if (user == null) return "redirect:/admin/login";

        List<Notification> all = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        long unreadCount = notificationRepository.countByUser_IdAndIsReadFalse(user.getId());

        // Paginate
        int total      = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));
        int from = (page - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        model.addAttribute("notifications", all.subList(from, to));
        model.addAttribute("unreadCount",   unreadCount);
        model.addAttribute("totalCount",    total);
        model.addAttribute("currentPage",   page);
        model.addAttribute("totalPages",    totalPages);
        model.addAttribute("pageTitle",     "Notifications");
        return "admin/notifications";
    }

    /**
     * POST: đánh dấu 1 thông báo đã đọc rồi chuyển hướng về đúng trang.
     * Dùng khi click từ trang full-list (không qua JS Fetch).
     */
    @PostMapping("/notifications/{id}/read-and-go")
    public String readAndGo(@PathVariable String id,
                            @RequestParam(value = "target", defaultValue = "/admin/notifications") String target,
                            Authentication authentication) {
        User user = resolveUser(authentication);
        if (user != null) {
            notificationRepository.findById(id)
                    .filter(n -> n.getUser().getId().equals(user.getId()))
                    .ifPresent(n -> {
                        n.setRead(true);
                        notificationRepository.save(n);
                    });
        }
        return "redirect:" + target;
    }

    // ======================== Helpers ========================

    private User resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElse(null);
    }

    private Map<String, Object> toDto(Notification n) {
        return Map.of(
                "id",             n.getId(),
                "title",          n.getTitle(),
                "message",        n.getMessage(),
                "type",           n.getType().name(),
                "isRead",         n.isRead(),
                "createdAt",      n.getCreatedAt().toString(),
                "relatedOrderId", n.getRelatedOrderId() != null ? n.getRelatedOrderId() : "",
                "relatedBookId",  n.getRelatedBookId()  != null ? n.getRelatedBookId()  : ""
        );
    }
}
