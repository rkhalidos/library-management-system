package com.library.strategy;

import com.library.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Search strategy that searches books by ISBN.
 * Performs exact or partial matching on ISBN.
 */
public class ISBNSearchStrategy implements SearchStrategy {

    @Override
    public List<Book> search(List<Book> books, String query) {
        List<Book> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery = normalizeIsbn(query);
        for (Book book : books) {
            String normalizedIsbn = normalizeIsbn(book.getIsbn());
            if (normalizedIsbn.contains(normalizedQuery) || normalizedIsbn.equals(normalizedQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    /**
     * Normalizes ISBN by removing hyphens and spaces.
     *
     * @param isbn ISBN to normalize
     * @return Normalized ISBN
     */
    private String normalizeIsbn(String isbn) {
        return isbn.replaceAll("[\\s-]", "").toLowerCase();
    }

    @Override
    public String getStrategyName() {
        return "ISBN Search";
    }
}
