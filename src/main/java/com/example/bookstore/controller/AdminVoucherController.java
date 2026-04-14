package com.example.bookstore.controller;

import com.example.bookstore.entity.Voucher;
import com.example.bookstore.service.VoucherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Voucher (mã giảm giá) dành cho Admin.
 */
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final VoucherService voucherService;

    public AdminVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.findAll());
        model.addAttribute("pageTitle", "Manage Vouchers");
        return "admin/vouchers";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("pageTitle", "Create Voucher");
        model.addAttribute("isEdit", false);
        return "admin/voucher-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        return voucherService.findById(id)
                .map(voucher -> {
                    model.addAttribute("voucher", voucher);
                    model.addAttribute("pageTitle", "Edit Voucher");
                    model.addAttribute("isEdit", true);
                    return "admin/voucher-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Voucher not found with ID: " + id);
                    return "redirect:/admin/vouchers";
                });
    }

    @PostMapping("/save")
    public String saveVoucher(@ModelAttribute("voucher") Voucher voucher,
                              RedirectAttributes redirectAttributes) {
        try {
            voucherService.save(voucher);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Voucher \"" + voucher.getCode() + "\" saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable("id") String id,
                                RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle/{id}")
    public String toggleVoucher(@PathVariable("id") String id,
                                RedirectAttributes redirectAttributes) {
        try {
            voucherService.toggleActive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher status updated!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }
}
