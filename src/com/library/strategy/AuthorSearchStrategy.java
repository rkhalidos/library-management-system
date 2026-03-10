package com.library.strategy;

import com.library.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Search strategy that searches books by author.
 * Performs case-insensitive partial matching.
 */
public class AuthorSearchStrategy implements SearchStrategy {

    @Override
    public List<Book> search(List<Book> books, String query) {
        List<Book> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String lowerQuery = query.toLowerCase().trim();
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public String getStrategyName() {
        return "Author Search";
    }
}
