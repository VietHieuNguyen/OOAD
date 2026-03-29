package com.example.bookstore.controller;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.CloudinaryService;
import com.example.bookstore.service.WishlistService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CloudinaryService cloudinaryService;
    private final WishlistService wishlistService;

    public ProfileController(UserRepository userRepository,
                             CustomerRepository customerRepository,
                             CloudinaryService cloudinaryService,
                             WishlistService wishlistService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.cloudinaryService = cloudinaryService;
        this.wishlistService = wishlistService;
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

        // Wishlist — lấy từ DB
        List<Wishlist> wishlist = wishlistService.getWishlistByCustomer(user.getId());
        model.addAttribute("wishlist", wishlist);

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

    /**
     * Toggle wishlist (thêm/xóa sách khỏi danh sách yêu thích).
     */
    @PostMapping("/wishlist/toggle")
    public String toggleWishlist(@ModelAttribute("currentUser") User currentUser,
                                 @RequestParam String bookId,
                                 @RequestParam(defaultValue = "/collections") String redirectUrl,
                                 RedirectAttributes redirectAttributes) {
        if (currentUser == null) return "redirect:/login";

        Customer customer = customerRepository.findById(currentUser.getId()).orElse(null);
        if (customer == null) return "redirect:/";

        boolean added = wishlistService.toggleWishlist(customer, bookId);
        redirectAttributes.addFlashAttribute(
                added ? "success" : "info",
                added ? "Đã thêm vào Wishlist!" : "Đã xóa khỏi Wishlist."
        );

        return "redirect:" + redirectUrl;
    }

    /**
     * Xóa item khỏi wishlist (từ trang Profile).
     */
    @PostMapping("/wishlist/remove/{id}")
    public String removeFromWishlist(@PathVariable String id) {
        wishlistService.removeFromWishlist(id);
        return "redirect:/profile";
    }
}
