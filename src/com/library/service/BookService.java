package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.factory.BookFactory;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.observer.NotificationService;
import com.library.repository.BookRepository;
import com.library.strategy.*;
import com.library.util.LibraryLogger;

import java.util.*;

/**
 * Service class for book management operations.
 * Handles CRUD operations, search functionality, and inventory tracking.
 */
public class BookService {
    private final BookRepository bookRepository;
    private final NotificationService notificationService;
    private SearchStrategy searchStrategy;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.notificationService = NotificationService.getInstance();
        this.searchStrategy = new CompositeSearchStrategy(); // Default search
    }

    /**
     * Adds a new book to the library.
     *
     * @param isbn            Book ISBN
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @param genre           Book genre
     * @return The added book
     */
    public Book addBook(String isbn, String title, String author, int publicationYear, String genre) {
        if (bookRepository.existsById(isbn)) {
            throw new IllegalArgumentException("Book with ISBN " + isbn + " already exists");
        }
        
        Book book = BookFactory.createBook(isbn, title, author, publicationYear, genre);
        bookRepository.save(book);
        
        // Notify subscribers about new book
        notificationService.broadcastNewBook(title, author, genre);
        
        LibraryLogger.logBookEvent("ADD", isbn, String.format("'%s' by %s added to library", title, author));
        return book;
    }

    /**
     * Adds an existing book object to the library.
     *
     * @param book The book to add
     * @return The added book
     */
    public Book addBook(Book book) {
        if (bookRepository.existsById(book.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        
        bookRepository.save(book);
        notificationService.broadcastNewBook(book.getTitle(), book.getAuthor(), book.getGenre());
        
        LibraryLogger.logBookEvent("ADD", book.getIsbn(), 
            String.format("'%s' by %s added to library", book.getTitle(), book.getAuthor()));
        return book;
    }

    /**
     * Removes a book from the library.
     *
     * @param isbn ISBN of the book to remove
     * @return true if the book was removed
     * @throws BookNotFoundException if book not found
     */
    public boolean removeBook(String isbn) {
        Book book = getBookByIsbn(isbn);
        
        if (book.getStatus() == BookStatus.BORROWED) {
            throw new IllegalStateException("Cannot remove a borrowed book");
        }
        
        boolean removed = bookRepository.deleteById(isbn);
        if (removed) {
            LibraryLogger.logBookEvent("REMOVE", isbn, "Book removed from library");
        }
        return removed;
    }

    /**
     * Updates book information.
     *
     * @param isbn            ISBN of the book to update
     * @param title           New title (null to keep current)
     * @param author          New author (null to keep current)
     * @param publicationYear New year (0 to keep current)
     * @param genre           New genre (null to keep current)
     * @return The updated book
     * @throws BookNotFoundException if book not found
     */
    public Book updateBook(String isbn, String title, String author, int publicationYear, String genre) {
        Book book = getBookByIsbn(isbn);
        
        if (title != null && !title.trim().isEmpty()) {
            book.setTitle(title);
        }
        if (author != null && !author.trim().isEmpty()) {
            book.setAuthor(author);
        }
        if (publicationYear > 0) {
            book.setPublicationYear(publicationYear);
        }
        if (genre != null && !genre.trim().isEmpty()) {
            book.setGenre(genre);
        }
        
        bookRepository.save(book);
        LibraryLogger.logBookEvent("UPDATE", isbn, "Book information updated");
        return book;
    }

    /**
     * Gets a book by ISBN.
     *
     * @param isbn Book ISBN
     * @return The book
     * @throws BookNotFoundException if book not found
     */
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    /**
     * Gets all books in the library.
     *
     * @return List of all books
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Gets all available books.
     *
     * @return List of available books
     */
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailable();
    }

    /**
     * Searches for books using the current search strategy.
     *
     * @param query Search query
     * @return List of matching books
     */
    public List<Book> searchBooks(String query) {
        List<Book> allBooks = bookRepository.findAll();
        return searchStrategy.search(allBooks, query);
    }

    /**
     * Searches for books by title.
     *
     * @param title Title to search for
     * @return List of matching books
     */
    public List<Book> searchByTitle(String title) {
        SearchStrategy titleStrategy = new TitleSearchStrategy();
        return titleStrategy.search(bookRepository.findAll(), title);
    }

    /**
     * Searches for books by author.
     *
     * @param author Author to search for
     * @return List of matching books
     */
    public List<Book> searchByAuthor(String author) {
        SearchStrategy authorStrategy = new AuthorSearchStrategy();
        return authorStrategy.search(bookRepository.findAll(), author);
    }

    /**
     * Searches for books by ISBN.
     *
     * @param isbn ISBN to search for
     * @return List of matching books
     */
    public List<Book> searchByIsbn(String isbn) {
        SearchStrategy isbnStrategy = new ISBNSearchStrategy();
        return isbnStrategy.search(bookRepository.findAll(), isbn);
    }

    /**
     * Searches for books by genre.
     *
     * @param genre Genre to search for
     * @return List of matching books
     */
    public List<Book> searchByGenre(String genre) {
        SearchStrategy genreStrategy = new GenreSearchStrategy();
        return genreStrategy.search(bookRepository.findAll(), genre);
    }

    /**
     * Sets the search strategy to use.
     *
     * @param strategy The search strategy
     */
    public void setSearchStrategy(SearchStrategy strategy) {
        this.searchStrategy = strategy;
        LibraryLogger.info(LibraryLogger.BOOK, "Search strategy changed to: " + strategy.getStrategyName());
    }

    /**
     * Gets books by status.
     *
     * @param status Book status
     * @return List of books with the status
     */
    public List<Book> getBooksByStatus(BookStatus status) {
        return bookRepository.findByStatus(status);
    }

    /**
     * Gets books in a specific branch.
     *
     * @param branchId Branch ID
     * @return List of books in the branch
     */
    public List<Book> getBooksByBranch(String branchId) {
        return bookRepository.findByBranch(branchId);
    }

    /**
     * Gets all unique genres.
     *
     * @return Set of genres
     */
    public Set<String> getAllGenres() {
        return bookRepository.getAllGenres();
    }

    /**
     * Gets all unique authors.
     *
     * @return Set of authors
     */
    public Set<String> getAllAuthors() {
        return bookRepository.getAllAuthors();
    }

    /**
     * Gets total book count.
     *
     * @return Total number of books
     */
    public int getTotalBookCount() {
        return bookRepository.count();
    }

    /**
     * Gets available book count.
     *
     * @return Number of available books
     */
    public int getAvailableBookCount() {
        return bookRepository.countAvailable();
    }

    /**
     * Gets borrowed book count.
     *
     * @return Number of borrowed books
     */
    public int getBorrowedBookCount() {
        return bookRepository.countBorrowed();
    }

    /**
     * Checks if a book exists.
     *
     * @param isbn Book ISBN
     * @return true if book exists
     */
    public boolean bookExists(String isbn) {
        return bookRepository.existsById(isbn);
    }

    /**
     * Gets a summary of library inventory.
     *
     * @return Map with inventory statistics
     */
    public Map<String, Integer> getInventorySummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("Total Books", bookRepository.count());
        summary.put("Available", bookRepository.countAvailable());
        summary.put("Borrowed", bookRepository.countBorrowed());
        summary.put("Unique Genres", bookRepository.getAllGenres().size());
        summary.put("Unique Authors", bookRepository.getAllAuthors().size());
        return summary;
    }
}
