package com.library.strategy;

import com.library.model.Book;

import java.util.List;

/**
 * Strategy interface for book search operations.
 * Implements the Strategy design pattern for flexible search algorithms.
 */
public interface SearchStrategy {
    /**
     * Searches for books matching the given query.
     *
     * @param books Collection of books to search
     * @param query Search query
     * @return List of matching books
     */
    List<Book> search(List<Book> books, String query);

    /**
     * Gets the name of this search strategy.
     *
     * @return Strategy name
     */
    String getStrategyName();
}
