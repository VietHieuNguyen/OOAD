package com.example.bookstore.controller;

import com.example.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "oauth2Error", required = false) String oauth2Error,
                            Model model) {
        if (oauth2Error != null) {
            model.addAttribute("error", resolveOauth2ErrorMessage(oauth2Error));
        } else if (error != null) {
            model.addAttribute("error", "Ten dang nhap hoac mat khau khong dung.");
        }
        if (logout != null) {
            model.addAttribute("success", "Da dang xuat thanh cong.");
        }
        model.addAttribute("googleLoginEnabled", googleClientId != null && !googleClientId.isBlank());
        return "client/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("googleLoginEnabled", googleClientId != null && !googleClientId.isBlank());
        return "client/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String fullName,
                               @RequestParam(required = false) String phoneNumber,
                               @RequestParam(required = false) String address,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.registerCustomer(username, email, password, fullName, phoneNumber, address);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
            redirectAttributes.addFlashAttribute("address", address);
            return "redirect:/register";
        }
    }

    private String resolveOauth2ErrorMessage(String oauth2Error) {
        return switch (oauth2Error) {
            case "google_session_expired" -> "Phiên đăng nhập Google đã hết hạn hoặc cookie xác thực đã bị mất. Vui lòng bấm lại nút Google và thử lại.";
            case "google_invalid_grant" -> "Mã xác thực Google đã hết hạn hoặc đã bị dừng lại. Vui lòng đăng nhập Google từ đầu, không refresh hay mở lại link callback cũ.";
            case "google_invalid_client" -> "Cấu hình Google OAuth đang sai client secret hoặc client id.";
            case "google_access_denied" -> "Bạn đã hủy hoặc từ chối quyền đăng nhập bằng Google.";
            case "google_connection_failed" -> "Không kết nối được tới Google để hoàn tất đăng nhập.";
            case "google_account_sync_failed" -> "Đăng nhập Google thành công nhưng không lưu được tài khoản vào database.";
            case "invalid_google_account" -> "Tài khoản Google không hợp lệ hoặc thiếu email.";
            case "account_disabled" -> "Tài khoản của bạn đã bị khóa.";
            default -> "Đăng nhập Google thất bại. Vui lòng thử lại.";
        };
    }
}
