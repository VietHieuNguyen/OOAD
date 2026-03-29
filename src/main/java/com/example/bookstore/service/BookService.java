package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        List<Book> books = bookRepository.findAll();
        // Force init lazy-loaded category trong transaction
        books.forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return books;
    }

    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        Page<Book> page = bookRepository.findAll(pageable);
        page.getContent().forEach(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return page;
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(String id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        bookOpt.ifPresent(b -> {
            if (b.getCategory() != null) b.getCategory().getName();
        });
        return bookOpt;
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
}
