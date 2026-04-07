package com.example.bookstore.controller;

import com.example.bookstore.service.CloudinaryService;
import com.example.bookstore.service.SiteSettingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý cài đặt chung (General Settings) dành cho Admin.
 * <p>Cho phép Admin thay đổi: Site Name, Hero Image.</p>
 */
@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final SiteSettingService siteSettingService;
    private final CloudinaryService cloudinaryService;

    public AdminSettingsController(SiteSettingService siteSettingService,
                                   CloudinaryService cloudinaryService) {
        this.siteSettingService = siteSettingService;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Hiển thị trang Settings.
     */
    @GetMapping
    public String settingsPage(Model model) {
        model.addAttribute("siteName", siteSettingService.getSiteName());
        model.addAttribute("heroImageUrl", siteSettingService.getHeroImageUrl());
        model.addAttribute("pageTitle", "Site Settings");
        return "admin/settings";
    }

    /**
     * Lưu settings (Site Name + Hero Image).
     */
    @PostMapping
    public String saveSettings(@RequestParam(value = "siteName", required = false) String siteName,
                               @RequestParam(value = "heroImageUrl", required = false) String heroImageUrl,
                               @RequestParam(value = "heroImageFile", required = false) MultipartFile heroImageFile,
                               RedirectAttributes redirectAttributes) {
        try {
            // Lưu Site Name
            if (siteName != null && !siteName.isBlank()) {
                siteSettingService.saveSiteName(siteName);
            }

            // Hero Image: ưu tiên file upload, fallback URL
            if (heroImageFile != null && !heroImageFile.isEmpty()) {
                String uploadedUrl = cloudinaryService.uploadImage(heroImageFile, "bookstore/settings");
                siteSettingService.saveHeroImageUrl(uploadedUrl);
            } else if (heroImageUrl != null && !heroImageUrl.isBlank()) {
                siteSettingService.saveHeroImageUrl(heroImageUrl);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu cài đặt thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu cài đặt: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }
}
