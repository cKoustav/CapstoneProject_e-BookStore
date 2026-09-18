package com.ebookstore.controller;

import com.ebookstore.dto.ApiResponse;
import com.ebookstore.model.Book;
import com.ebookstore.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        List<Book> books;
        if (search != null && !search.isBlank()) {
            books = bookService.searchBooks(search);
        } else if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            books = bookService.getBooksByCategory(category);
        } else {
            books = bookService.getAllBooks();
        }
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable String id) {
        return bookService.getBookById(id)
                .map(book -> ResponseEntity.ok(ApiResponse.success(book, "Book found")))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Book not found with ID: " + id)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(bookService.getAllCategories(), "Categories retrieved"));
    }
}
