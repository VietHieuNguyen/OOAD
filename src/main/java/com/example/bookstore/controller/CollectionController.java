package com.example.bookstore.controller;

import com.example.bookstore.entity.Book;
import com.example.bookstore.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller trang Collections — tuân thủ MVC (Controller → Service → Repository).
 */
@Controller
public class CollectionController {

    private final BookService bookService;

    public CollectionController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/collections")
    public String collectionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model) {

        Sort sortObj = dir.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        Page<Book> booksPage = bookService.findAll(PageRequest.of(page, size, sortObj));

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("totalItems", booksPage.getTotalElements());
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        return "client/collections";
    }
}
