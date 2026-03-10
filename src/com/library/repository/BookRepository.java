package com.library.repository;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.util.LibraryLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository implementation for Book entities.
 * Provides in-memory storage with thread-safe operations.
 */
public class BookRepository implements Repository<Book, String> {
    private final Map<String, Book> books;

    public BookRepository() {
        this.books = new ConcurrentHashMap<>();
    }

    @Override
    public Book save(Book book) {
        books.put(book.getIsbn(), book);
        LibraryLogger.logBookEvent("SAVE", book.getIsbn(), book.getTitle());
        return book;
    }

    @Override
    public Optional<Book> findById(String isbn) {
        return Optional.ofNullable(books.get(isbn));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public boolean deleteById(String isbn) {
        Book removed = books.remove(isbn);
        if (removed != null) {
            LibraryLogger.logBookEvent("DELETE", isbn, removed.getTitle());
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(String isbn) {
        return books.containsKey(isbn);
    }

    @Override
    public int count() {
        return books.size();
    }

    @Override
    public void deleteAll() {
        books.clear();
        LibraryLogger.info(LibraryLogger.BOOK, "All books deleted from repository");
    }

    /**
     * Finds all available books.
     *
     * @return List of available books
     */
    public List<Book> findAvailable() {
        return books.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Finds all books by status.
     *
     * @param status Book status to filter by
     * @return List of books with the given status
     */
    public List<Book> findByStatus(BookStatus status) {
        return books.values().stream()
                .filter(book -> book.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Finds all books in a specific branch.
     *
     * @param branchId Branch ID
     * @return List of books in the branch
     */
    public List<Book> findByBranch(String branchId) {
        return books.values().stream()
                .filter(book -> branchId.equals(book.getBranchId()))
                .collect(Collectors.toList());
    }

    /**
     * Finds all books by genre.
     *
     * @param genre Genre to search for
     * @return List of books in the genre
     */
    public List<Book> findByGenre(String genre) {
        return books.values().stream()
                .filter(book -> genre.equalsIgnoreCase(book.getGenre()))
                .collect(Collectors.toList());
    }

    /**
     * Finds all books by author.
     *
     * @param author Author name
     * @return List of books by the author
     */
    public List<Book> findByAuthor(String author) {
        return books.values().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets count of available books.
     *
     * @return Number of available books
     */
    public int countAvailable() {
        return (int) books.values().stream()
                .filter(Book::isAvailable)
                .count();
    }

    /**
     * Gets count of borrowed books.
     *
     * @return Number of borrowed books
     */
    public int countBorrowed() {
        return (int) books.values().stream()
                .filter(book -> book.getStatus() == BookStatus.BORROWED)
                .count();
    }

    /**
     * Gets all unique genres in the repository.
     *
     * @return Set of genres
     */
    public Set<String> getAllGenres() {
        return books.values().stream()
                .map(Book::getGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all unique authors in the repository.
     *
     * @return Set of authors
     */
    public Set<String> getAllAuthors() {
        return books.values().stream()
                .map(Book::getAuthor)
                .collect(Collectors.toSet());
    }
}
