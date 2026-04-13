package com.example.bookstore.controller;

import com.example.bookstore.entity.Category;
import com.example.bookstore.service.CategoryService;
import com.example.bookstore.service.CloudinaryService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quan ly danh muc (Category CRUD) danh cho Admin.
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    public AdminCategoryController(CategoryService categoryService,
                                   CloudinaryService cloudinaryService) {
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
    }

    // ======================== LIST (READ) ========================

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();

        java.util.Map<String, Long> bookCounts = new java.util.LinkedHashMap<>();
        for (Category cat : categories) {
            bookCounts.put(cat.getId(), categoryService.countBooksInCategory(cat.getId()));
        }

        model.addAttribute("categories", categories);
        model.addAttribute("allCategories", categories); // for parent dropdown in modal
        model.addAttribute("bookCounts", bookCounts);
        model.addAttribute("pageTitle", "Manage Categories");
        return "admin/categories";
    }

    // ======================== DETAIL VIEW ========================

    @GetMapping("/{id}")
    public String viewCategory(@PathVariable("id") String id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        return categoryService.findById(id)
                .map(category -> {
                    var books = categoryService.findBooksInCategory(id);
                    model.addAttribute("category", category);
                    model.addAttribute("books", books);
                    model.addAttribute("pageTitle", category.getName());
                    return "admin/category-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Khong tim thay danh muc voi ID: " + id);
                    return "redirect:/admin/categories";
                });
    }

    // ======================== CREATE ========================

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("allCategories", categoryService.findAll());
        model.addAttribute("pageTitle", "Add New Category");
        model.addAttribute("isEdit", false);
        return "admin/category-form";
    }

    // ======================== UPDATE ========================

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        return categoryService.findById(id)
                .map(category -> {
                    model.addAttribute("category", category);
                    model.addAttribute("allCategories", categoryService.findAll());
                    model.addAttribute("pageTitle", "Edit Category");
                    model.addAttribute("isEdit", true);
                    return "admin/category-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Category not found with ID: " + id);
                    return "redirect:/admin/categories";
                });
    }

    // ======================== SAVE (CREATE or UPDATE) ========================

    /**
     * Xu ly luu danh muc (POST).
     * Hỗ trợ upload ảnh từ file (Cloudinary) hoặc nhập URL trực tiếp.
     */
    @PostMapping("/save")
    public String saveCategory(@RequestParam(value = "id", required = false) String id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "imageUrl", required = false) String imageUrl,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "parentId", required = false) String parentId,
                               @RequestParam(value = "isActive", required = false) Boolean isActive,
                               @RequestParam(value = "returnTo", required = false) String returnTo,
                               RedirectAttributes redirectAttributes) {
        Category category;

        if (id != null && !id.isBlank()) {
            category = categoryService.findById(id).orElse(new Category());
            category.setId(id);
        } else {
            category = new Category();
        }

        category.setName(name.trim());
        category.setIsActive(isActive != null ? isActive : false);

        // Set parent category
        if (parentId != null && !parentId.isBlank()) {
            categoryService.findById(parentId).ifPresent(category::setParentCategory);
        } else {
            category.setParentCategory(null);
        }

        // Upload ảnh từ file nếu có (ưu tiên hơn URL)
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploadedUrl = cloudinaryService.uploadImage(imageFile, "bookstore/categories");
                category.setImageUrl(uploadedUrl);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Loi upload anh: " + e.getMessage());
                return "redirect:/admin/categories";
            }
        } else {
            category.setImageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl.trim() : null);
        }

        Category saved = categoryService.save(category);

        redirectAttributes.addFlashAttribute("successMessage",
                "Da luu danh muc \"" + name.trim() + "\" thanh cong!");

        if ("detail".equals(returnTo) && saved.getId() != null) {
            return "redirect:/admin/categories/" + saved.getId();
        }
        return "redirect:/admin/categories";
    }

    // ======================== DELETE ========================

    /**
     * Xoa danh muc theo ID (POST).
     * <p>Luong: POST /admin/categories/delete/{id}
     * → kiem tra rang buoc (con sach trong danh muc nay khong?)
     * → neu con sach: tra ve thong bao loi bang Flash attribute (Alert do)
     * → neu rong: xoa thanh cong, tra ve thong bao thanh cong (Alert xanh)
     * → redirect ve danh sach.</p>
     */
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") String id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Da xoa danh muc thanh cong!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ======================== QUICK-ADD (AJAX from book form modal) ========================

    /**
     * AJAX endpoint: quickly create a category from the book-form modal.
     * Returns JSON: {id, name} on success or {error} on failure.
     */
    @PostMapping("/quick-add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAdd(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String name = body.get("name");
            if (name == null || name.isBlank()) {
                result.put("error", "Name is required.");
                return ResponseEntity.badRequest().body(result);
            }
            Category cat = new Category();
            cat.setName(name.trim());
            String imageUrl = body.get("imageUrl");
            if (imageUrl != null && !imageUrl.isBlank()) {
                cat.setImageUrl(imageUrl.trim());
            }
            Category saved = categoryService.save(cat);
            result.put("id", saved.getId());
            result.put("name", saved.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}


