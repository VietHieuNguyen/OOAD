package com.example.bookstore.controller;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.CustomerAddress;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.repository.CustomerAddressRepository;
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
    private final CustomerAddressRepository addressRepository;
    private final CloudinaryService cloudinaryService;
    private final WishlistService wishlistService;

    public ProfileController(UserRepository userRepository,
                             CustomerRepository customerRepository,
                             CustomerAddressRepository addressRepository,
                             CloudinaryService cloudinaryService,
                             WishlistService wishlistService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.cloudinaryService = cloudinaryService;
        this.wishlistService = wishlistService;
    }

    // ─── Profile page ───────────────────────────────────────────

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String principalName = authentication.getName();
        User user = userRepository.findByUsername(principalName)
                .or(() -> userRepository.findByEmail(principalName))
                .orElse(null);

        if (user == null) return "redirect:/login?error=user_not_found";

        model.addAttribute("user", user);

        // Wishlist
        List<Wishlist> wishlist = wishlistService.getWishlistByCustomer(user.getId());
        model.addAttribute("wishlist", wishlist);

        // Customer & addresses
        Customer customer = customerRepository.findById(user.getId()).orElse(null);
        model.addAttribute("customer", customer);

        if (customer != null) {
            List<CustomerAddress> addresses =
                    addressRepository.findByCustomerIdOrderByIsDefaultDescIdAsc(user.getId());
            model.addAttribute("addresses", addresses);
        } else {
            model.addAttribute("addresses", List.of());
        }

        return "client/profile";
    }

    // ─── Avatar ─────────────────────────────────────────────────

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile file,
                               Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";

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
                return "redirect:/profile?error=upload_failed";
            }
        }
        return "redirect:/profile";
    }

    // ─── Address: ADD ────────────────────────────────────────────

    @PostMapping("/profile/address/add")
    public String addAddress(@ModelAttribute("currentUser") User currentUser,
                             @RequestParam("recipientName") String recipientName,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam("address") String address,
                             @RequestParam(value = "city", required = false) String city,
                             @RequestParam(value = "label", required = false) String label,
                             @RequestParam(value = "makeDefault", required = false) Boolean makeDefault,
                             RedirectAttributes redirectAttributes) {
        if (currentUser == null) return "redirect:/login";

        Customer customer = customerRepository.findById(currentUser.getId()).orElse(null);
        if (customer == null) return "redirect:/login";

        // If this is the first address or makeDefault is true → clear old default
        long count = addressRepository.countByCustomerId(customer.getId());
        boolean setDefault = (count == 0) || Boolean.TRUE.equals(makeDefault);

        if (setDefault) {
            addressRepository.clearDefaultForCustomer(customer.getId());
        }

        CustomerAddress addr = new CustomerAddress();
        addr.setCustomer(customer);
        addr.setRecipientName(recipientName.trim());
        addr.setPhone(phone != null ? phone.trim() : null);
        addr.setAddress(address.trim());
        addr.setCity(city != null ? city.trim() : null);
        addr.setLabel(label != null && !label.isBlank() ? label.trim() : "Home");
        addr.setIsDefault(setDefault);
        addressRepository.save(addr);

        redirectAttributes.addFlashAttribute("addressSuccess", "New address added successfully!");
        return "redirect:/profile#address-book";
    }

    // ─── Address: SET DEFAULT ────────────────────────────────────

    @PostMapping("/profile/address/{id}/default")
    public String setDefaultAddress(@ModelAttribute("currentUser") User currentUser,
                                    @PathVariable("id") String addressId,
                                    RedirectAttributes redirectAttributes) {
        if (currentUser == null) return "redirect:/login";

        CustomerAddress addr = addressRepository.findById(addressId).orElse(null);
        if (addr == null || !addr.getCustomer().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("addressError", "Address not found.");
            return "redirect:/profile#address-book";
        }

        addressRepository.clearDefaultForCustomer(currentUser.getId());
        addr.setIsDefault(true);
        addressRepository.save(addr);

        redirectAttributes.addFlashAttribute("addressSuccess", "Default address updated.");
        return "redirect:/profile#address-book";
    }

    // ─── Address: DELETE ────────────────────────────────────────

    @PostMapping("/profile/address/{id}/delete")
    public String deleteAddress(@ModelAttribute("currentUser") User currentUser,
                                @PathVariable("id") String addressId,
                                RedirectAttributes redirectAttributes) {
        if (currentUser == null) return "redirect:/login";

        CustomerAddress addr = addressRepository.findById(addressId).orElse(null);
        if (addr != null && addr.getCustomer().getId().equals(currentUser.getId())) {
            addressRepository.delete(addr);
            redirectAttributes.addFlashAttribute("addressSuccess", "Address deleted.");
        }
        return "redirect:/profile#address-book";
    }

    // ─── Wishlist ────────────────────────────────────────────────

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
                added ? "Added to Wishlist!" : "Removed from Wishlist."
        );
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/wishlist/remove/{id}")
    public String removeFromWishlist(@PathVariable String id) {
        wishlistService.removeFromWishlist(id);
        return "redirect:/profile";
    }
}
