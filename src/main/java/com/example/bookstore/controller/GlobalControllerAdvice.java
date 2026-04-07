package com.example.bookstore.controller;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.SiteSettingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = { "com.example.bookstore.controller" })
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final SiteSettingService siteSettingService;

    public GlobalControllerAdvice(UserRepository userRepository,
                                  SiteSettingService siteSettingService) {
        this.userRepository = userRepository;
        this.siteSettingService = siteSettingService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String name = authentication.getName();
            return userRepository.findByUsername(name)
                    .or(() -> userRepository.findByEmail(name))
                    .orElse(null);
        }
        return null;
    }

    @ModelAttribute("siteName")
    public String getSiteName() {
        return siteSettingService.getSiteName();
    }

    @ModelAttribute("heroImageUrl")
    public String getHeroImageUrl() {
        return siteSettingService.getHeroImageUrl();
    }
}

