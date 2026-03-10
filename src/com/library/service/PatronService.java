package com.library.service;

import com.library.exception.PatronNotFoundException;
import com.library.factory.PatronFactory;
import com.library.model.LoanRecord;
import com.library.model.Patron;
import com.library.model.PatronType;
import com.library.observer.NotificationService;
import com.library.repository.LoanRepository;
import com.library.repository.PatronRepository;
import com.library.util.LibraryLogger;

import java.util.*;

/**
 * Service class for patron management operations.
 * Handles patron CRUD, borrowing history, and notifications.
 */
public class PatronService {
    private final PatronRepository patronRepository;
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;

    public PatronService(PatronRepository patronRepository, LoanRepository loanRepository) {
        this.patronRepository = patronRepository;
        this.loanRepository = loanRepository;
        this.notificationService = NotificationService.getInstance();
    }

    /**
     * Registers a new patron.
     *
     * @param name       Patron name
     * @param email      Patron email
     * @param patronType Type of patron
     * @return The registered patron
     */
    public Patron registerPatron(String name, String email, PatronType patronType) {
        // Check if email already exists
        if (patronRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A patron with email " + email + " already exists");
        }
        
        Patron patron = PatronFactory.createPatron(name, email, patronType);
        patronRepository.save(patron);
        
        // Subscribe to new book notifications
        notificationService.subscribe(NotificationService.EVENT_NEW_BOOK_ADDED, patron);
        
        LibraryLogger.logPatronEvent("REGISTER", patron.getPatronId(), 
            String.format("%s registered as %s", name, patronType));
        return patron;
    }

    /**
     * Registers a new regular patron.
     *
     * @param name  Patron name
     * @param email Patron email
     * @return The registered patron
     */
    public Patron registerPatron(String name, String email) {
        return registerPatron(name, email, PatronType.REGULAR);
    }

    /**
     * Adds an existing patron to the system.
     *
     * @param patron The patron to add
     * @return The added patron
     */
    public Patron addPatron(Patron patron) {
        if (patronRepository.existsById(patron.getPatronId())) {
            throw new IllegalArgumentException("Patron with ID " + patron.getPatronId() + " already exists");
        }
        
        patronRepository.save(patron);
        notificationService.subscribe(NotificationService.EVENT_NEW_BOOK_ADDED, patron);
        
        LibraryLogger.logPatronEvent("ADD", patron.getPatronId(), patron.getName());
        return patron;
    }

    /**
     * Updates patron information.
     *
     * @param patronId   ID of patron to update
     * @param name       New name (null to keep current)
     * @param email      New email (null to keep current)
     * @param phone      New phone (null to keep current)
     * @param patronType New type (null to keep current)
     * @return The updated patron
     * @throws PatronNotFoundException if patron not found
     */
    public Patron updatePatron(String patronId, String name, String email, String phone, PatronType patronType) {
        Patron patron = getPatronById(patronId);
        
        if (name != null && !name.trim().isEmpty()) {
            patron.setName(name);
        }
        if (email != null && !email.trim().isEmpty()) {
            // Check if new email is already in use by another patron
            Optional<Patron> existingPatron = patronRepository.findByEmail(email);
            if (existingPatron.isPresent() && !existingPatron.get().getPatronId().equals(patronId)) {
                throw new IllegalArgumentException("Email " + email + " is already in use");
            }
            patron.setEmail(email);
        }
        if (phone != null) {
            patron.setPhone(phone);
        }
        if (patronType != null) {
            patron.setPatronType(patronType);
        }
        
        patronRepository.save(patron);
        LibraryLogger.logPatronEvent("UPDATE", patronId, "Patron information updated");
        return patron;
    }

    /**
     * Removes a patron from the system.
     *
     * @param patronId ID of patron to remove
     * @return true if patron was removed
     * @throws PatronNotFoundException if patron not found
     */
    public boolean removePatron(String patronId) {
        Patron patron = getPatronById(patronId);
        
        // Check if patron has active loans
        if (patron.getCurrentBorrowedCount() > 0) {
            throw new IllegalStateException("Cannot remove patron with active loans");
        }
        
        // Unsubscribe from notifications
        notificationService.detach(patron);
        
        boolean removed = patronRepository.deleteById(patronId);
        if (removed) {
            LibraryLogger.logPatronEvent("REMOVE", patronId, "Patron removed from system");
        }
        return removed;
    }

    /**
     * Gets a patron by ID.
     *
     * @param patronId Patron ID
     * @return The patron
     * @throws PatronNotFoundException if patron not found
     */
    public Patron getPatronById(String patronId) {
        return patronRepository.findById(patronId)
                .orElseThrow(() -> new PatronNotFoundException(patronId));
    }

    /**
     * Gets a patron by email.
     *
     * @param email Patron email
     * @return Optional containing the patron if found
     */
    public Optional<Patron> getPatronByEmail(String email) {
        return patronRepository.findByEmail(email);
    }

    /**
     * Gets all patrons.
     *
     * @return List of all patrons
     */
    public List<Patron> getAllPatrons() {
        return patronRepository.findAll();
    }

    /**
     * Searches for patrons by name.
     *
     * @param name Name to search for
     * @return List of matching patrons
     */
    public List<Patron> searchPatronsByName(String name) {
        return patronRepository.findByName(name);
    }

    /**
     * Gets patrons by type.
     *
     * @param patronType Patron type
     * @return List of patrons with the type
     */
    public List<Patron> getPatronsByType(PatronType patronType) {
        return patronRepository.findByType(patronType);
    }

    /**
     * Gets the borrowing history for a patron.
     *
     * @param patronId Patron ID
     * @return List of loan records
     * @throws PatronNotFoundException if patron not found
     */
    public List<LoanRecord> getBorrowingHistory(String patronId) {
        getPatronById(patronId); // Verify patron exists
        return loanRepository.getBorrowingHistory(patronId);
    }

    /**
     * Gets active loans for a patron.
     *
     * @param patronId Patron ID
     * @return List of active loan records
     * @throws PatronNotFoundException if patron not found
     */
    public List<LoanRecord> getActiveLoans(String patronId) {
        getPatronById(patronId); // Verify patron exists
        return loanRepository.findActiveByPatronId(patronId);
    }

    /**
     * Adds a preference to a patron.
     *
     * @param patronId   Patron ID
     * @param preference Preference to add (genre or author)
     * @throws PatronNotFoundException if patron not found
     */
    public void addPreference(String patronId, String preference) {
        Patron patron = getPatronById(patronId);
        patron.addPreference(preference);
        patronRepository.save(patron);
        LibraryLogger.logPatronEvent("PREFERENCE", patronId, "Added preference: " + preference);
    }

    /**
     * Removes a preference from a patron.
     *
     * @param patronId   Patron ID
     * @param preference Preference to remove
     * @throws PatronNotFoundException if patron not found
     */
    public void removePreference(String patronId, String preference) {
        Patron patron = getPatronById(patronId);
        patron.removePreference(preference);
        patronRepository.save(patron);
        LibraryLogger.logPatronEvent("PREFERENCE", patronId, "Removed preference: " + preference);
    }

    /**
     * Gets patron preferences.
     *
     * @param patronId Patron ID
     * @return Set of preferences
     * @throws PatronNotFoundException if patron not found
     */
    public Set<String> getPreferences(String patronId) {
        Patron patron = getPatronById(patronId);
        return patron.getPreferences();
    }

    /**
     * Gets notifications for a patron.
     *
     * @param patronId Patron ID
     * @return List of notifications
     * @throws PatronNotFoundException if patron not found
     */
    public List<String> getNotifications(String patronId) {
        Patron patron = getPatronById(patronId);
        return patron.getNotifications();
    }

    /**
     * Clears notifications for a patron.
     *
     * @param patronId Patron ID
     * @throws PatronNotFoundException if patron not found
     */
    public void clearNotifications(String patronId) {
        Patron patron = getPatronById(patronId);
        patron.clearNotifications();
        patronRepository.save(patron);
    }

    /**
     * Checks if a patron can borrow more books.
     *
     * @param patronId Patron ID
     * @return true if patron can borrow
     * @throws PatronNotFoundException if patron not found
     */
    public boolean canBorrow(String patronId) {
        Patron patron = getPatronById(patronId);
        return patron.canBorrow();
    }

    /**
     * Gets the number of books a patron can still borrow.
     *
     * @param patronId Patron ID
     * @return Number of books that can still be borrowed
     * @throws PatronNotFoundException if patron not found
     */
    public int getRemainingBorrowCapacity(String patronId) {
        Patron patron = getPatronById(patronId);
        return patron.getMaxBooks() - patron.getCurrentBorrowedCount();
    }

    /**
     * Gets total patron count.
     *
     * @return Total number of patrons
     */
    public int getTotalPatronCount() {
        return patronRepository.count();
    }

    /**
     * Gets patron count by type.
     *
     * @param patronType Patron type
     * @return Number of patrons of the type
     */
    public int getPatronCountByType(PatronType patronType) {
        return patronRepository.countByType(patronType);
    }

    /**
     * Checks if a patron exists.
     *
     * @param patronId Patron ID
     * @return true if patron exists
     */
    public boolean patronExists(String patronId) {
        return patronRepository.existsById(patronId);
    }

    /**
     * Gets patrons with active loans.
     *
     * @return List of patrons with active loans
     */
    public List<Patron> getPatronsWithActiveLoans() {
        return patronRepository.findWithActiveLoans();
    }

    /**
     * Gets a summary of patron statistics.
     *
     * @return Map with patron statistics
     */
    public Map<String, Integer> getPatronSummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("Total Patrons", patronRepository.count());
        summary.put("Regular", patronRepository.countByType(PatronType.REGULAR));
        summary.put("Students", patronRepository.countByType(PatronType.STUDENT));
        summary.put("Faculty", patronRepository.countByType(PatronType.FACULTY));
        summary.put("Seniors", patronRepository.countByType(PatronType.SENIOR));
        summary.put("With Active Loans", patronRepository.findWithActiveLoans().size());
        return summary;
    }
}
