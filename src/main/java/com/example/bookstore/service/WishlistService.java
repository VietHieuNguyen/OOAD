package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.WishlistRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           BookRepository bookRepository) {
        this.wishlistRepository = wishlistRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Lấy danh sách wishlist của customer, init lazy Book + Category.
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistByCustomer(String customerId) {
        List<Wishlist> list = wishlistRepository.findByCustomer_IdOrderByAddedAtDesc(customerId);
        // Force init lazy Book (open-in-view=false)
        list.forEach(w -> {
            w.getBook().getTitle();
            if (w.getBook().getCategory() != null) {
                w.getBook().getCategory().getName();
            }
        });
        return list;
    }

    /**
     * Toggle wishlist: nếu đã có thì xóa, chưa có thì thêm.
     * @return true nếu đã thêm, false nếu đã xóa
     */
    @Transactional
    public boolean toggleWishlist(Customer customer, String bookId) {
        var existing = wishlistRepository.findByCustomer_IdAndBook_BookId(customer.getId(), bookId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false; // đã xóa
        } else {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found."));
            Wishlist w = new Wishlist();
            w.setCustomer(customer);
            w.setBook(book);
            wishlistRepository.save(w);
            return true; // đã thêm
        }
    }

    /**
     * Kiểm tra sách đã trong wishlist chưa.
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(String customerId, String bookId) {
        return wishlistRepository.existsByCustomer_IdAndBook_BookId(customerId, bookId);
    }

    /**
     * Xóa 1 item khỏi wishlist.
     */
    @Transactional
    public void removeFromWishlist(String wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }
}
