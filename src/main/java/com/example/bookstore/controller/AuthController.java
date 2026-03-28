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
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("success", "Đã đăng xuất thành công!");
        }
        // Enable Google login button if client-id is configured
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
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập.");
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
}
