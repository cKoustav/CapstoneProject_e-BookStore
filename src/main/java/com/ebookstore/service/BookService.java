package com.ebookstore.service;

import com.ebookstore.model.Book;
import com.ebookstore.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @PostConstruct
    public void seedInitialBooks() {
        if (bookRepository.count() == 0) {
            List<Book> books = new ArrayList<>();

            books.add(new Book(
                    "book-1",
                    "Clean Code: A Handbook of Agile Software Craftsmanship",
                    "Robert C. Martin",
                    "9780132350884",
                    "Programming",
                    "Even bad code can function. But if code is not clean, it can bring a development organization to its knees. Learn the principles of clean craftsmanship.",
                    new BigDecimal("34.99"),
                    25,
                    "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-2",
                    "Effective Java",
                    "Joshua Bloch",
                    "9780134685991",
                    "Programming",
                    "The definitive guide to Java platform best practices—updated for Java 7, 8, and 9. Highly recommended for every serious Java engineer.",
                    new BigDecimal("42.50"),
                    30,
                    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-3",
                    "Spring Boot in Action",
                    "Craig Walls",
                    "9781617292545",
                    "Programming",
                    "Master modern enterprise application development with Spring Boot. Covers microservices, security, and cloud deployment.",
                    new BigDecimal("39.99"),
                    20,
                    "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-4",
                    "Designing Data-Intensive Applications",
                    "Martin Kleppmann",
                    "9781449373320",
                    "Architecture",
                    "The big ideas behind reliable, scalable, and maintainable systems. Key concepts of distributed databases, stream processing, and fault tolerance.",
                    new BigDecimal("49.95"),
                    18,
                    "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-5",
                    "The Pragmatic Programmer",
                    "David Thomas, Andrew Hunt",
                    "9780135957059",
                    "Career & Practice",
                    "20th Anniversary Edition. Your journey to mastery in software development through practical tips, mindset shifts, and continuous learning.",
                    new BigDecimal("38.00"),
                    15,
                    "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-6",
                    "Atomic Habits",
                    "James Clear",
                    "9780735211292",
                    "Self-Help",
                    "An easy and proven way to build good habits and break bad ones. Small changes make remarkable results.",
                    new BigDecimal("21.99"),
                    40,
                    "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-7",
                    "Dune",
                    "Frank Herbert",
                    "9780441013593",
                    "Sci-Fi & Fantasy",
                    "Set on the desert planet Arrakis, Dune tells the story of the boy Paul Atreides and the epic struggle for control of the galaxy's most valuable resource.",
                    new BigDecimal("18.50"),
                    35,
                    "https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&auto=format&fit=crop&q=60"
            ));

            books.add(new Book(
                    "book-8",
                    "To Kill a Mockingbird",
                    "Harper Lee",
                    "9780061120084",
                    "Fiction",
                    "A timeless masterpiece of American literature depicting justice, compassion, and courage in the American South.",
                    new BigDecimal("14.99"),
                    50,
                    "https://images.unsplash.com/photo-1474932430478-367dbb6832c1?w=400&auto=format&fit=crop&q=60"
            ));

            bookRepository.saveAll(books);
        }
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.search(query);
    }

    public List<String> getAllCategories() {
        return bookRepository.findAll().stream()
                .map(Book::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public Book saveBook(Book book) {
        if (book.getId() == null || book.getId().isBlank()) {
            book.setId(UUID.randomUUID().toString());
        }
        return bookRepository.save(book);
    }

    public boolean deleteBook(String id) {
        return bookRepository.deleteById(id);
    }
}
