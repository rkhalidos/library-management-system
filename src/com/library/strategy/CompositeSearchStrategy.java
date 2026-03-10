package com.library.strategy;

import com.library.model.Book;

import java.util.*;

/**
 * Composite search strategy that combines multiple search strategies.
 * Searches across all fields (title, author, ISBN, genre) simultaneously.
 */
public class CompositeSearchStrategy implements SearchStrategy {
    private final List<SearchStrategy> strategies;

    /**
     * Creates a composite strategy with default search strategies.
     */
    public CompositeSearchStrategy() {
        this.strategies = new ArrayList<>();
        strategies.add(new TitleSearchStrategy());
        strategies.add(new AuthorSearchStrategy());
        strategies.add(new ISBNSearchStrategy());
        strategies.add(new GenreSearchStrategy());
    }

    /**
     * Creates a composite strategy with custom search strategies.
     *
     * @param strategies List of search strategies to use
     */
    public CompositeSearchStrategy(List<SearchStrategy> strategies) {
        this.strategies = new ArrayList<>(strategies);
    }

    @Override
    public List<Book> search(List<Book> books, String query) {
        Set<Book> results = new LinkedHashSet<>(); // Preserve order, avoid duplicates
        
        for (SearchStrategy strategy : strategies) {
            List<Book> strategyResults = strategy.search(books, query);
            results.addAll(strategyResults);
        }
        
        return new ArrayList<>(results);
    }

    /**
     * Adds a search strategy to the composite.
     *
     * @param strategy Strategy to add
     */
    public void addStrategy(SearchStrategy strategy) {
        strategies.add(strategy);
    }

    /**
     * Removes a search strategy from the composite.
     *
     * @param strategy Strategy to remove
     */
    public void removeStrategy(SearchStrategy strategy) {
        strategies.remove(strategy);
    }

    @Override
    public String getStrategyName() {
        return "Composite Search (All Fields)";
    }
}
