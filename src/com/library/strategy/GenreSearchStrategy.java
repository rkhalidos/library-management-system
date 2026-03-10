package com.library.strategy;

import com.library.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Search strategy that searches books by genre.
 * Performs case-insensitive matching.
 */
public class GenreSearchStrategy implements SearchStrategy {

    @Override
    public List<Book> search(List<Book> books, String query) {
        List<Book> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String lowerQuery = query.toLowerCase().trim();
        for (Book book : books) {
            if (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public String getStrategyName() {
        return "Genre Search";
    }
}
