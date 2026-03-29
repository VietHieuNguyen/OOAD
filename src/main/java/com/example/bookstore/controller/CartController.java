package com.example.bookstore.controller;

import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.service.CartService;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý giỏ hàng (Cart).
 *
 * <p>Tích hợp <b>Decorator Pattern</b> thông qua {@link CartService#calculatePriceBreakdown(Cart)}
 * để hiển thị chi tiết từng dòng giá trên giao diện.</p>
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CustomerRepository customerRepository;

    public CartController(CartService cartService,
                          CustomerRepository customerRepository) {
        this.cartService = cartService;
        this.customerRepository = customerRepository;
    }

    /**
     * Hiển thị trang giỏ hàng.
     * Sử dụng Decorator Pattern để tính chi tiết giá.
     */
    @GetMapping
    public String cartPage(@ModelAttribute("currentUser") User currentUser,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Customer customer = findCustomer(currentUser);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/";
        }

        Cart cart = cartService.getOrCreateCart(customer);

        // Decorator Pattern — Tính chi tiết giá
        Map<String, BigDecimal> priceBreakdown = cartService.calculatePriceBreakdown(cart);

        model.addAttribute("cart", cart);
        model.addAttribute("priceBreakdown", priceBreakdown);

        return "client/cart";
    }

    /**
     * Thêm sách vào giỏ hàng.
     */
    @PostMapping("/add")
    public String addToCart(@ModelAttribute("currentUser") User currentUser,
                           @RequestParam String bookId,
                           @RequestParam(defaultValue = "1") int quantity,
                           RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Customer customer = findCustomer(currentUser);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng.");
            return "redirect:/";
        }

        try {
            cartService.addCartItem(customer, bookId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sách vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng.
     */
    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable String itemId) {
        cartService.removeCartItem(itemId);
        return "redirect:/cart";
    }

    /**
     * Cập nhật số lượng sản phẩm.
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam String cartItemId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }

    /**
     * Áp dụng mã giảm giá (Voucher).
     * Kích hoạt VoucherDiscountDecorator khi render lại trang.
     */
    @PostMapping("/apply-voucher")
    public String applyVoucher(@ModelAttribute("currentUser") User currentUser,
                               @RequestParam String voucherCode,
                               RedirectAttributes redirectAttributes) {
        if (currentUser == null) return "redirect:/login";

        Customer customer = findCustomer(currentUser);
        if (customer == null) return "redirect:/";

        Cart cart = cartService.getOrCreateCart(customer);
        String error = cartService.applyVoucher(cart, voucherCode);

        if (error != null) {
            redirectAttributes.addFlashAttribute("voucherError", error);
        } else {
            redirectAttributes.addFlashAttribute("voucherSuccess", "Áp dụng mã giảm giá thành công!");
        }

        return "redirect:/cart";
    }

    /**
     * Hủy mã giảm giá.
     */
    @PostMapping("/remove-voucher")
    public String removeVoucher(@ModelAttribute("currentUser") User currentUser) {
        if (currentUser == null) return "redirect:/login";

        Customer customer = findCustomer(currentUser);
        if (customer == null) return "redirect:/";

        Cart cart = cartService.getOrCreateCart(customer);
        cartService.removeVoucher(cart);

        return "redirect:/cart";
    }

    /**
     * Bật/tắt gói quà. Kích hoạt/hủy GiftWrapDecorator.
     */
    @PostMapping("/toggle-gift-wrap")
    public String toggleGiftWrap(@ModelAttribute("currentUser") User currentUser) {
        if (currentUser == null) return "redirect:/login";

        Customer customer = findCustomer(currentUser);
        if (customer == null) return "redirect:/";

        Cart cart = cartService.getOrCreateCart(customer);
        cartService.toggleGiftWrap(cart);

        return "redirect:/cart";
    }

    private Customer findCustomer(User user) {
        return customerRepository.findById(user.getId()).orElse(null);
    }
}
