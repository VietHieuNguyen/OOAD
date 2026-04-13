package com.example.bookstore.controller;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller for book detail — passes isWishlisted flag so the wishlist button
 * shows the correct state (Add vs Remove).
 */
@Controller
public class BookDetailController {

    private final BookService bookService;
    private final WishlistService wishlistService;

    public BookDetailController(BookService bookService, WishlistService wishlistService) {
        this.bookService = bookService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/books/{id}")
    public String bookDetailPage(@PathVariable String id,
                                 @ModelAttribute("currentUser") User currentUser,
                                 Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        model.addAttribute("book", book);

        // Wishlist state — only check if user is logged in
        boolean isWishlisted = false;
        if (currentUser != null) {
            isWishlisted = wishlistService.isInWishlist(currentUser.getId(), id);
        }
        model.addAttribute("isWishlisted", isWishlisted);

        // Lấy sách liên quan (cùng category)
        if (book.getCategory() != null) {
            model.addAttribute("relatedBooks", bookService.findRelatedBooks(book.getCategory().getId(), book.getId(), 5));
        }

        return "client/book-detail";
    }
}
