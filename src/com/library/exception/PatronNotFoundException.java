package com.library.exception;

/**
 * Exception thrown when a patron is not found in the library system.
 */
public class PatronNotFoundException extends LibraryException {
    
    public PatronNotFoundException(String patronId) {
        super("Patron not found with ID: " + patronId);
    }
    
    public PatronNotFoundException(String message, boolean custom) {
        super(message);
    }
}
