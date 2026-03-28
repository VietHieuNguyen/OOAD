package com.example.bookstore.controller;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.CloudinaryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public ProfileController(UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String principalName = authentication.getName();
        User user = userRepository.findByUsername(principalName)
                .or(() -> userRepository.findByEmail(principalName))
                .orElse(null);

        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("user", user);
        return "client/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String principalName = authentication.getName();
        User user = userRepository.findByUsername(principalName)
                .or(() -> userRepository.findByEmail(principalName))
                .orElse(null);

        if (user != null && !file.isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadAvatar(file);
                user.setAvatar(avatarUrl);
                userRepository.save(user);
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/profile?error=upload_failed";
            }
        }
        return "redirect:/profile";
    }

    @GetMapping("/test-cloudinary")
    @org.springframework.web.bind.annotation.ResponseBody
    public String testCloudinary() {
        try {
            return "Cloudinary credentials loaded? " + (cloudinaryService != null ? "Yes" : "No");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
