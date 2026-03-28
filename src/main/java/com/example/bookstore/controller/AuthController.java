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
            redirectAttributes.addFlashAttribute("success", "Dang ky thanh cong. Hay dang nhap.");
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
            case "google_session_expired" -> "Phien dang nhap Google da het han hoac cookie xac thuc da bi mat. Hay bam lai nut Google va thu lai.";
            case "google_invalid_grant" -> "Ma xac thuc Google da het han hoac da bi dung lai. Hay dang nhap Google tu dau, khong refresh hay mo lai link callback cu.";
            case "google_invalid_client" -> "Cau hinh Google OAuth dang sai client secret hoac client id.";
            case "google_access_denied" -> "Ban da huy hoac tu choi quyen dang nhap bang Google.";
            case "google_connection_failed" -> "Khong ket noi duoc toi Google de hoan tat dang nhap.";
            case "google_account_sync_failed" -> "Dang nhap Google thanh cong nhung khong luu duoc tai khoan vao database.";
            case "invalid_google_account" -> "Tai khoan Google khong hop le hoac thieu email.";
            case "account_disabled" -> "Tai khoan cua ban da bi khoa.";
            default -> "Dang nhap Google that bai. Vui long thu lai.";
        };
    }
}
