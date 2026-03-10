package com.library.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Centralized logging utility for the library management system.
 * Provides consistent logging across all components.
 */
public class LibraryLogger {
    private static final Logger LOGGER = Logger.getLogger("LibraryManagementSystem");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean initialized = false;

    // Log categories
    public static final String BOOK = "BOOK";
    public static final String PATRON = "PATRON";
    public static final String LOAN = "LOAN";
    public static final String RESERVATION = "RESERVATION";
    public static final String BRANCH = "BRANCH";
    public static final String SYSTEM = "SYSTEM";

    /**
     * Initializes the logger with console handler.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);

        // Console handler with custom formatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] [%s] %s%n",
                        LocalDateTime.now().format(FORMATTER),
                        record.getLevel().getName(),
                        record.getMessage());
            }
        });

        LOGGER.addHandler(consoleHandler);
        initialized = true;
        
        info(SYSTEM, "Library Management System Logger initialized");
    }

    /**
     * Logs an info message.
     *
     * @param category Log category
     * @param message  Log message
     */
    public static void info(String category, String message) {
        ensureInitialized();
        LOGGER.info(String.format("[%s] %s", category, message));
    }

    /**
     * Logs a warning message.
     *
     * @param category Log category
     * @param message  Log message
     */
    public static void warn(String category, String message) {
        ensureInitialized();
        LOGGER.warning(String.format("[%s] %s", category, message));
    }

    /**
     * Logs an error message.
     *
     * @param category Log category
     * @param message  Log message
     */
    public static void error(String category, String message) {
        ensureInitialized();
        LOGGER.severe(String.format("[%s] %s", category, message));
    }

    /**
     * Logs an error message with exception.
     *
     * @param category  Log category
     * @param message   Log message
     * @param throwable Exception to log
     */
    public static void error(String category, String message, Throwable throwable) {
        ensureInitialized();
        LOGGER.log(Level.SEVERE, String.format("[%s] %s", category, message), throwable);
    }

    /**
     * Logs a debug message.
     *
     * @param category Log category
     * @param message  Log message
     */
    public static void debug(String category, String message) {
        ensureInitialized();
        LOGGER.fine(String.format("[%s] %s", category, message));
    }

    /**
     * Logs a book-related event.
     *
     * @param action Action performed (ADD, REMOVE, UPDATE, etc.)
     * @param isbn   Book ISBN
     * @param detail Additional details
     */
    public static void logBookEvent(String action, String isbn, String detail) {
        info(BOOK, String.format("%s - ISBN: %s - %s", action, isbn, detail));
    }

    /**
     * Logs a patron-related event.
     *
     * @param action   Action performed
     * @param patronId Patron ID
     * @param detail   Additional details
     */
    public static void logPatronEvent(String action, String patronId, String detail) {
        info(PATRON, String.format("%s - PatronID: %s - %s", action, patronId, detail));
    }

    /**
     * Logs a loan-related event.
     *
     * @param action   Action performed (CHECKOUT, RETURN, etc.)
     * @param isbn     Book ISBN
     * @param patronId Patron ID
     * @param detail   Additional details
     */
    public static void logLoanEvent(String action, String isbn, String patronId, String detail) {
        info(LOAN, String.format("%s - Book: %s - Patron: %s - %s", action, isbn, patronId, detail));
    }

    /**
     * Logs a reservation-related event.
     *
     * @param action   Action performed
     * @param isbn     Book ISBN
     * @param patronId Patron ID
     * @param detail   Additional details
     */
    public static void logReservationEvent(String action, String isbn, String patronId, String detail) {
        info(RESERVATION, String.format("%s - Book: %s - Patron: %s - %s", action, isbn, patronId, detail));
    }

    /**
     * Logs a branch-related event.
     *
     * @param action   Action performed
     * @param branchId Branch ID
     * @param detail   Additional details
     */
    public static void logBranchEvent(String action, String branchId, String detail) {
        info(BRANCH, String.format("%s - Branch: %s - %s", action, branchId, detail));
    }

    /**
     * Ensures the logger is initialized.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
