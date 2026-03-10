package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.PatronNotFoundException;
import com.library.model.*;
import com.library.observer.NotificationService;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.PatronRepository;
import com.library.util.LibraryLogger;

import java.util.List;
import java.util.Optional;

/**
 * Service class for lending operations.
 * Handles book checkout, return, and loan management.
 */
public class LendingService {
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;
    private ReservationService reservationService;

    public LendingService(BookRepository bookRepository, PatronRepository patronRepository, 
                          LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.loanRepository = loanRepository;
        this.notificationService = NotificationService.getInstance();
    }

    /**
     * Sets the reservation service for handling reservations on book return.
     *
     * @param reservationService The reservation service
     */
    public void setReservationService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Checks out a book to a patron.
     *
     * @param isbn     ISBN of the book to checkout
     * @param patronId ID of the patron borrowing the book
     * @return The loan record
     * @throws BookNotFoundException      if book not found
     * @throws PatronNotFoundException    if patron not found
     * @throws BookNotAvailableException  if book is not available
     */
    public LoanRecord checkoutBook(String isbn, String patronId) {
        // Validate book exists
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        
        // Validate patron exists
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new PatronNotFoundException(patronId));
        
        // Check if book is available
        if (!book.isAvailable()) {
            throw new BookNotAvailableException(isbn, "Book is currently " + book.getStatus());
        }
        
        // Check if patron can borrow more books
        if (!patron.canBorrow()) {
            throw new IllegalStateException(
                String.format("Patron has reached maximum borrowing limit (%d books)", patron.getMaxBooks()));
        }
        
        // Create loan record with patron-specific loan period
        LoanRecord loanRecord = new LoanRecord(isbn, patronId, patron.getLoanPeriodDays());
        
        // Update book status
        book.setStatus(BookStatus.BORROWED);
        bookRepository.save(book);
        
        // Update patron's borrowed count and add loan to history
        patron.addLoanRecord(loanRecord);
        patronRepository.save(patron);
        
        // Save loan record
        loanRepository.save(loanRecord);
        
        LibraryLogger.logLoanEvent("CHECKOUT", isbn, patronId, 
            String.format("Due: %s", loanRecord.getDueDate()));
        
        return loanRecord;
    }

    /**
     * Returns a borrowed book.
     *
     * @param isbn     ISBN of the book being returned
     * @param patronId ID of the patron returning the book
     * @return The updated loan record
     * @throws BookNotFoundException   if book not found
     * @throws PatronNotFoundException if patron not found
     */
    public LoanRecord returnBook(String isbn, String patronId) {
        // Validate book exists
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        
        // Validate patron exists
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new PatronNotFoundException(patronId));
        
        // Find the active loan
        LoanRecord loanRecord = loanRepository.findActiveByBookIsbn(isbn)
                .orElseThrow(() -> new IllegalStateException("No active loan found for book: " + isbn));
        
        // Verify the loan belongs to this patron
        if (!loanRecord.getPatronId().equals(patronId)) {
            throw new IllegalStateException("This book was not borrowed by this patron");
        }
        
        // Mark loan as returned
        loanRecord.markReturned();
        loanRepository.save(loanRecord);
        
        // Update patron's borrowed count
        patron.returnBook();
        patronRepository.save(patron);
        
        // Check if there are pending reservations for this book
        if (reservationService != null) {
            Optional<Reservation> nextReservation = reservationService.getNextPendingReservation(isbn);
            if (nextReservation.isPresent()) {
                // Mark book as reserved and notify the patron
                book.setStatus(BookStatus.RESERVED);
                Reservation reservation = nextReservation.get();
                reservationService.processReservationReady(reservation.getReservationId());
                
                LibraryLogger.logReservationEvent("READY", isbn, reservation.getPatronId(),
                    "Book available for pickup");
            } else {
                // No reservations, book is available
                book.setStatus(BookStatus.AVAILABLE);
            }
        } else {
            book.setStatus(BookStatus.AVAILABLE);
        }
        
        bookRepository.save(book);
        
        boolean wasOverdue = loanRecord.getStatus() == LoanStatus.OVERDUE;
        LibraryLogger.logLoanEvent("RETURN", isbn, patronId, 
            wasOverdue ? "Returned OVERDUE" : "Returned on time");
        
        return loanRecord;
    }

    /**
     * Gets all active loans.
     *
     * @return List of active loan records
     */
    public List<LoanRecord> getActiveLoans() {
        return loanRepository.findActiveLoans();
    }

    /**
     * Gets all overdue loans.
     *
     * @return List of overdue loan records
     */
    public List<LoanRecord> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }

    /**
     * Gets active loans for a specific patron.
     *
     * @param patronId Patron ID
     * @return List of active loan records for the patron
     */
    public List<LoanRecord> getPatronActiveLoans(String patronId) {
        return loanRepository.findActiveByPatronId(patronId);
    }

    /**
     * Gets the active loan for a specific book.
     *
     * @param isbn Book ISBN
     * @return Optional containing the active loan if found
     */
    public Optional<LoanRecord> getBookActiveLoan(String isbn) {
        return loanRepository.findActiveByBookIsbn(isbn);
    }

    /**
     * Checks if a book is currently borrowed.
     *
     * @param isbn Book ISBN
     * @return true if book is borrowed
     */
    public boolean isBookBorrowed(String isbn) {
        return loanRepository.findActiveByBookIsbn(isbn).isPresent();
    }

    /**
     * Gets loan by ID.
     *
     * @param loanId Loan ID
     * @return Optional containing the loan if found
     */
    public Optional<LoanRecord> getLoanById(String loanId) {
        return loanRepository.findById(loanId);
    }

    /**
     * Sends due date reminders for loans due soon.
     *
     * @param daysThreshold Number of days before due date to send reminder
     */
    public void sendDueDateReminders(int daysThreshold) {
        List<LoanRecord> activeLoans = loanRepository.findActiveLoans();
        
        for (LoanRecord loan : activeLoans) {
            long daysUntilDue = loan.getDaysUntilDue();
            
            if (daysUntilDue > 0 && daysUntilDue <= daysThreshold) {
                // Get patron and book details
                patronRepository.findById(loan.getPatronId()).ifPresent(patron -> {
                    bookRepository.findById(loan.getBookIsbn()).ifPresent(book -> {
                        notificationService.notifyDueDateReminder(patron, book.getTitle(), daysUntilDue);
                    });
                });
            }
        }
        
        LibraryLogger.info(LibraryLogger.LOAN, "Due date reminders sent");
    }

    /**
     * Sends overdue notifications.
     */
    public void sendOverdueNotifications() {
        List<LoanRecord> overdueLoans = loanRepository.findOverdueLoans();
        
        for (LoanRecord loan : overdueLoans) {
            long daysOverdue = -loan.getDaysUntilDue(); // Negative because it's past due
            
            patronRepository.findById(loan.getPatronId()).ifPresent(patron -> {
                bookRepository.findById(loan.getBookIsbn()).ifPresent(book -> {
                    notificationService.notifyOverdue(patron, book.getTitle(), daysOverdue);
                });
            });
        }
        
        LibraryLogger.info(LibraryLogger.LOAN, 
            String.format("Overdue notifications sent for %d loans", overdueLoans.size()));
    }

    /**
     * Gets count of active loans.
     *
     * @return Number of active loans
     */
    public int getActiveLoanCount() {
        return loanRepository.countActive();
    }

    /**
     * Gets count of overdue loans.
     *
     * @return Number of overdue loans
     */
    public int getOverdueLoanCount() {
        return loanRepository.countOverdue();
    }

    /**
     * Gets total loan count (including returned).
     *
     * @return Total number of loans
     */
    public int getTotalLoanCount() {
        return loanRepository.count();
    }
}
