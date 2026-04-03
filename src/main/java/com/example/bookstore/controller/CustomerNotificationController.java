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
 * Controller thông báo phía Customer.
 *
 * <p>Cung cấp:
 * <ul>
 *   <li>REST API ({@code /api/notifications/**}) — cho popover chuông trên header client.</li>
 *   <li>MVC page ({@code /notifications}) — trang xem tất cả thông báo của customer.</li>
 * </ul>
 * Chỉ trả về thông báo thuộc về user đang đăng nhập (bảo mật theo userId).</p>
 */
@Controller
@RequestMapping
public class CustomerNotificationController {

    private static final int PREVIEW_SIZE = 8;
    private static final int PAGE_SIZE    = 12;

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;

    public CustomerNotificationController(NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository         = userRepository;
    }

    // ── REST API (popover JS) ──────────────────────────────────────────

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.status(401).build();

        List<Notification> all = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        List<Notification> preview = all.size() > PREVIEW_SIZE ? all.subList(0, PREVIEW_SIZE) : all;
        long unreadCount = notificationRepository.countByUser_IdAndIsReadFalse(user.getId());

        return ResponseEntity.ok(Map.of(
                "notifications", preview.stream().map(this::toDto).toList(),
                "unreadCount",   unreadCount,
                "totalCount",    (long) all.size()
        ));
    }

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

    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllRead(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) return ResponseEntity.status(401).build();

        List<Notification> unread = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);

        return ResponseEntity.ok(Map.of("marked", unread.size()));
    }

    // ── MVC Page ──────────────────────────────────────────────────────

    @GetMapping("/notifications")
    public String notificationsPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Authentication authentication,
            Model model) {

        User user = resolveUser(authentication);
        if (user == null) return "redirect:/login";

        List<Notification> all = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        long unreadCount = notificationRepository.countByUser_IdAndIsReadFalse(user.getId());

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
        return "client/notifications";
    }

    /**
     * Đánh dấu đã đọc rồi redirect (dùng từ trang MVC, không qua JS).
     */
    @PostMapping("/notifications/{id}/read-and-go")
    public String readAndGo(@PathVariable String id,
                            @RequestParam(value = "target", defaultValue = "/notifications") String target,
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

    // ── Helpers ───────────────────────────────────────────────────────

    private User resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) return null;
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
