package com.example.bookstore.controller;

import com.example.bookstore.service.ForgotPasswordService;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

@Controller
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "client/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            String normalizedEmail = forgotPasswordService.sendOtp(email);
            redirectAttributes.addFlashAttribute("success", "OTP da duoc gui toi email cua ban.");
            return "redirect:/forgot-password/verify?email=" + encode(normalizedEmail);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/forgot-password/verify")
    public String verifyOtpPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "client/forgot-password-verify";
    }

    @PostMapping("/forgot-password/verify")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes) {
        try {
            String normalizedEmail = forgotPasswordService.verifyOtp(email, otp);
            return "redirect:/forgot-password/reset?email=" + encode(normalizedEmail);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password/verify?email=" + encode(email);
        }
    }

    @GetMapping("/forgot-password/reset")
    public String resetPasswordPage(@RequestParam String email,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("email", forgotPasswordService.validateResetAccess(email));
            return "client/forgot-password-rest";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            forgotPasswordService.resetPassword(email, newPassword);
            redirectAttributes.addFlashAttribute("success", "Doi mat khau thanh cong. Hay dang nhap lai.");
            return "redirect:/login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password/reset?email=" + encode(email);
        }
    }

    private String encode(String value) {
        return UriUtils.encode(value, StandardCharsets.UTF_8);
    }
}
