package com.example.bookstore.controller;

import com.example.bookstore.entity.Category;
import com.example.bookstore.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quan ly danh muc (Category CRUD) danh cho Admin.
 * <p>
 * Luong hoat dong chinh (MVC):
 * <ol>
 *   <li>Admin truy cap <code>/admin/categories</code> → hien thi danh sach danh muc</li>
 *   <li>Bam "Add New Category" → mo Modal Bootstrap 5, nhap ten + ảnh URL</li>
 *   <li>Bam "Edit" → mo Modal voi du lieu cu de sua</li>
 *   <li>Submit Modal → POST <code>/admin/categories/save</code>, luu va redirect</li>
 *   <li>Bam "Delete" → POST <code>/admin/categories/delete/{id}</code>,
 *       kiem tra rang buoc (con sach hay khong) roi xoa hoac tra loi</li>
 * </ol>
 * </p>
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ======================== LIST (READ) ========================

    /**
     * Hien thi danh sach tat ca danh muc trong he thong.
     * <p>Luong: GET /admin/categories → query DB → tra ve view "admin/categories"
     * kem theo doi tuong Category rong cho Modal "Add New".</p>
     *
     * @param model Spring Model de truyen du lieu xuong Thymeleaf
     * @return ten view "admin/categories"
     */
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();

        // Tinh so sach cho moi danh muc va truyen xuong view
        // (Tránh goi lazy collection trong Thymeleaf)
        java.util.Map<String, Long> bookCounts = new java.util.LinkedHashMap<>();
        for (Category cat : categories) {
            bookCounts.put(cat.getId(), categoryService.countBooksInCategory(cat.getId()));
        }

        model.addAttribute("categories", categories);
        model.addAttribute("bookCounts", bookCounts);
        model.addAttribute("pageTitle", "Manage Categories");
        return "admin/categories";
    }

    // ======================== DETAIL VIEW ========================

    /**
     * Hien thi trang chi tiet danh muc: thong tin Category + danh sach sach ben trong.
     * <p>Luong: GET /admin/categories/{id}
     * → tim Category theo ID → lay danh sach Book thuoc Category do
     * → tra ve view "admin/category-detail".</p>
     *
     * <p>Tren trang nay, Admin co the:
     * <ul>
     *   <li>Chinh sua thong tin Category (ten, anh) qua form inline</li>
     *   <li>Xem danh sach sach thuoc danh muc</li>
     *   <li>Bam vao 1 cuon sach → chuyen den trang Edit Book</li>
     *   <li>Xoa sach khoi danh muc nay</li>
     * </ul></p>
     *
     * @param id ID cua danh muc can xem
     */
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

    // ======================== SAVE (CREATE or UPDATE) ========================

    /**
     * Xu ly luu danh muc (POST) – dung chung cho ca Tao moi va Cap nhat.
     * <p>Luong: POST /admin/categories/save → nhan du lieu tu Modal form
     * (id, name, imageUrl) → goi CategoryService.save() → redirect ve danh sach
     * hoac trang chi tiet (tuy thuoc tham so 'returnTo').</p>
     *
     * @param id        ID cua danh muc (rong neu tao moi)
     * @param name      Ten danh muc
     * @param imageUrl  URL anh minh hoa (co the rong)
     * @param returnTo  "detail" neu muon quay lai trang chi tiet, mac dinh ve list
     */
    @PostMapping("/save")
    public String saveCategory(@RequestParam(value = "id", required = false) String id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "imageUrl", required = false) String imageUrl,
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
        category.setImageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl.trim() : null);

        Category saved = categoryService.save(category);

        redirectAttributes.addFlashAttribute("successMessage",
                "Da luu danh muc \"" + name.trim() + "\" thanh cong!");

        // Redirect ve trang chi tiet neu duoc yeu cau
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
}

