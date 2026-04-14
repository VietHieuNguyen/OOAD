package com.example.bookstore.controller;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.CloudinaryService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller quan ly sach (CRUD) danh cho Admin.
 */
@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final BookService bookService;
    private final CloudinaryService cloudinaryService;

    public AdminBookController(BookService bookService, CloudinaryService cloudinaryService) {
        this.bookService = bookService;
        this.cloudinaryService = cloudinaryService;
    }

    // ======================== LIST (READ) ========================

    @GetMapping
    public String listBooks(@RequestParam(value = "keyword", required = false) String keyword,
                            Model model) {
        List<Book> books;
        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookService.searchByTitle(keyword.trim());
            model.addAttribute("keyword", keyword.trim());
        } else {
            books = bookService.findAll();
        }
        model.addAttribute("books", books);
        model.addAttribute("pageTitle", "Manage Books");
        return "admin/books";
    }

    // ======================== CREATE ========================

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("pageTitle", "Add New Book");
        model.addAttribute("isEdit", false);
        return "admin/book-form";
    }

    // ======================== UPDATE ========================

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        return bookService.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("categories", bookService.findAllCategories());
                    model.addAttribute("pageTitle", "Edit Book");
                    model.addAttribute("isEdit", true);
                    return "admin/book-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Khong tim thay sach voi ID: " + id);
                    return "redirect:/admin/books";
                });
    }

    // ======================== SAVE (CREATE or UPDATE) ========================

    /**
     * Xu ly luu sach (POST) – dung chung cho ca Tao moi va Cap nhat.
     * Hỗ trợ upload ảnh từ file (Cloudinary) hoặc nhập URL trực tiếp.
     */
    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") Book book,
                           @RequestParam("categoryId") String categoryId,
                           @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                           RedirectAttributes redirectAttributes) {
        // Gan Category cho Book truoc khi luu
        Category category = new Category();
        category.setId(categoryId);
        book.setCategory(category);

        // Mac dinh stockQuantity = 0 neu null
        if (book.getStockQuantity() == null) {
            book.setStockQuantity(0);
        }

        // Upload ảnh từ file nếu có (ưu tiên hơn URL)
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                String uploadedUrl = cloudinaryService.uploadImage(thumbnailFile, "bookstore/books");
                book.setThumbnail(uploadedUrl);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Loi upload anh: " + e.getMessage());
                return "redirect:/admin/books";
            }
        }

        try {
            bookService.save(book);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Da luu sach \"" + book.getTitle() + "\" thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Loi luu sach: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // ======================== TINYMCE UPLOAD ========================

    @PostMapping("/upload-tinymce")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadTinyMceImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadedUrl = cloudinaryService.uploadImage(file, "bookstore/tinymce");
            Map<String, String> response = new HashMap<>();
            response.put("location", uploadedUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ======================== DELETE ========================

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") String id,
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Da xoa sach thanh cong!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // ======================== TOGGLE ACTIVE ========================

    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable("id") String id,
                               RedirectAttributes redirectAttributes) {
        try {
            boolean newState = bookService.toggleActive(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    newState ? "Book activated." : "Book disabled (hidden from client).");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }
}
