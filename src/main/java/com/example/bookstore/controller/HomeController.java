package com.example.bookstore.controller;

import com.example.bookstore.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller trang chủ — tuân thủ MVC (Controller → Service → Repository).
 */
@Controller
public class HomeController {

    private final BookService bookService;

    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("books", bookService.findActiveForClient());
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("staffPicks", bookService.findStaffPicks());
        return "client/home";
    }
}
