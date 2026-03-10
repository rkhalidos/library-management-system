package com.library.exception;

/**
 * Exception thrown when a book is not found in the library system.
 */
public class BookNotFoundException extends LibraryException {
    
    public BookNotFoundException(String isbn) {
        super("Book not found with ISBN: " + isbn);
    }
    
    public BookNotFoundException(String message, boolean custom) {
        super(message);
    }
}
