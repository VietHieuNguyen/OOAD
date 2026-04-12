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
 * Controller for Collections page — category filter, search by title/slug, pagination.
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
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            Model model) {

        Sort sortObj = dir.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        PageRequest pageable = PageRequest.of(page, size, sortObj);

        Page<Book> booksPage;
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasQuery    = q != null && !q.isBlank();

        if (hasCategory && hasQuery) {
            booksPage = bookService.searchByCategoryAndQuery(category, q, pageable);
        } else if (hasCategory) {
            booksPage = bookService.findByCategoryPaged(category, pageable);
        } else if (hasQuery) {
            booksPage = bookService.searchByTitleOrSlug(q, pageable);
        } else {
            booksPage = bookService.findAll(pageable);
        }

        // Force-init lazy Category
        booksPage.getContent().forEach(b -> { if (b.getCategory() != null) b.getCategory().getName(); });

        model.addAttribute("books",       booksPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  booksPage.getTotalPages());
        model.addAttribute("totalItems",  booksPage.getTotalElements());
        model.addAttribute("categories",  bookService.findAllCategories());
        model.addAttribute("sort",        sort);
        model.addAttribute("dir",         dir);
        model.addAttribute("category",    category);
        model.addAttribute("q",           q);

        return "client/collections";
    }

    @GetMapping("/staff-picks")
    public String staffPicksPage(Model model) {
        model.addAttribute("staffPicks", bookService.findStaffPicks());
        model.addAttribute("categories", bookService.findAllCategories());
        return "client/staff-picks";
    }

    @GetMapping("/journals")
    public String journalsPage(Model model) {
        model.addAttribute("staffPicks", bookService.findStaffPicks());
        model.addAttribute("latestBooks", bookService.findLatest(6));
        return "client/journals";
    }
}
