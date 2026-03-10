package com.library.exception;

/**
 * Exception thrown when a book is not available for borrowing.
 */
public class BookNotAvailableException extends LibraryException {
    
    public BookNotAvailableException(String isbn) {
        super("Book is not available for borrowing. ISBN: " + isbn);
    }
    
    public BookNotAvailableException(String isbn, String reason) {
        super("Book (ISBN: " + isbn + ") is not available: " + reason);
    }
}
