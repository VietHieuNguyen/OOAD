package com.example.bookstore.controller;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.service.BookService;
import java.util.List;
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
 * Controller quan ly sach (CRUD) danh cho Admin.
 * <p>
 * Luong hoat dong chinh (MVC):
 * <ol>
 *   <li>Admin truy cap <code>/admin/books</code> → hien thi danh sach sach</li>
 *   <li>Bam "Add New Title" → chuyen den form tao moi tai <code>/admin/books/new</code></li>
 *   <li>Bam "Edit" tren 1 dong → chuyen den form sua tai <code>/admin/books/edit/{id}</code></li>
 *   <li>Bam "Delete" → goi POST <code>/admin/books/delete/{id}</code>, xoa va redirect</li>
 *   <li>Form submit (POST) → goi <code>/admin/books/save</code>, luu vao DB va redirect</li>
 * </ol>
 * </p>
 *
 * <p><b>Luu y cho thanh vien lam Observer Pattern:</b>
 * Logic thay doi stock nam trong {@link BookService#save(Book)} va
 * {@link BookService#updateStock(String, int)}. Ban chi can chen event/notify
 * o trong Service layer, khong can sua Controller nay.</p>
 */
@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ======================== LIST (READ) ========================

    /**
     * Hien thi danh sach tat ca sach trong he thong.
     * <p>Luong: GET /admin/books → query DB → tra ve view "admin/books".</p>
     *
     * @param keyword tu khoa tim kiem (optional)
     * @param model   Spring Model de truyen du lieu xuong Thymeleaf
     * @return ten view "admin/books"
     */
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

    /**
     * Hien thi form tao sach moi (GET).
     * <p>Luong: GET /admin/books/new → tao Book rong + load danh sach Category
     * → tra ve view "admin/book-form".</p>
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("pageTitle", "Add New Book");
        model.addAttribute("isEdit", false);
        return "admin/book-form";
    }

    // ======================== UPDATE ========================

    /**
     * Hien thi form chinh sua sach (GET).
     * <p>Luong: GET /admin/books/edit/{id} → tim Book theo ID
     * → neu khong tim thay redirect ve list voi thong bao loi
     * → tra ve view "admin/book-form" voi du lieu cu.</p>
     *
     * @param id ID cua sach can sua
     */
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
     * <p>Luong: POST /admin/books/save → bind du lieu tu form vao Book
     * → gan Category tu categoryId → goi BookService.save()
     * → redirect ve danh sach voi thong bao thanh cong.</p>
     *
     * @param book       doi tuong Book duoc Thymeleaf bind tu form
     * @param categoryId ID cua Category duoc chon tu dropdown
     */
    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") Book book,
                           @RequestParam("categoryId") String categoryId,
                           RedirectAttributes redirectAttributes) {
        // Gan Category cho Book truoc khi luu
        Category category = new Category();
        category.setId(categoryId);
        book.setCategory(category);

        // Mac dinh stockQuantity = 0 neu null
        if (book.getStockQuantity() == null) {
            book.setStockQuantity(0);
        }

        bookService.save(book);

        redirectAttributes.addFlashAttribute("successMessage",
                "Da luu sach \"" + book.getTitle() + "\" thanh cong!");
        return "redirect:/admin/books";
    }

    // ======================== DELETE ========================

    /**
     * Xoa sach theo ID (POST).
     * <p>Luong: POST /admin/books/delete/{id} → goi BookService.deleteById()
     * → redirect ve danh sach voi thong bao.</p>
     */
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
}
