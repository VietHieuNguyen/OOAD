package com.example.bookstore.controller;

import com.example.bookstore.entity.Book;
import com.example.bookstore.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller chi tiết sách — tuân thủ MVC (Controller → Service → Repository).
 */
@Controller
public class BookDetailController {

    private final BookService bookService;

    public BookDetailController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books/{id}")
    public String bookDetailPage(@PathVariable String id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));

        model.addAttribute("book", book);
        return "client/book-detail";
    }
}
