package com.ebookstore.repository;

import com.ebookstore.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository extends AbstractFileRepository<Book, String> {

    public BookRepository(@Value("${app.storage.directory:data}") String storageDir) {
        super(storageDir, "books.json", Book.class, Book::getId);
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) return Optional.empty();
        return findAll().stream()
                .filter(b -> isbn.equalsIgnoreCase(b.getIsbn()))
                .findFirst();
    }

    public List<Book> findByCategory(String category) {
        if (category == null || category.isBlank() || "all".equalsIgnoreCase(category)) {
            return findAll();
        }
        return findAll().stream()
                .filter(b -> category.equalsIgnoreCase(b.getCategory()))
                .toList();
    }

    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String q = query.toLowerCase().trim();
        return findAll().stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                        || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                        || (b.getCategory() != null && b.getCategory().toLowerCase().contains(q))
                        || (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(q)))
                .toList();
    }
}
