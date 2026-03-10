package com.library.repository;

import com.library.model.LoanRecord;
import com.library.model.LoanStatus;
import com.library.util.LibraryLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository implementation for LoanRecord entities.
 * Provides in-memory storage with thread-safe operations.
 */
public class LoanRepository implements Repository<LoanRecord, String> {
    private final Map<String, LoanRecord> loans;

    public LoanRepository() {
        this.loans = new ConcurrentHashMap<>();
    }

    @Override
    public LoanRecord save(LoanRecord loan) {
        loans.put(loan.getLoanId(), loan);
        LibraryLogger.logLoanEvent("SAVE", loan.getBookIsbn(), loan.getPatronId(), 
            "Status: " + loan.getStatus());
        return loan;
    }

    @Override
    public Optional<LoanRecord> findById(String loanId) {
        return Optional.ofNullable(loans.get(loanId));
    }

    @Override
    public List<LoanRecord> findAll() {
        return new ArrayList<>(loans.values());
    }

    @Override
    public boolean deleteById(String loanId) {
        LoanRecord removed = loans.remove(loanId);
        if (removed != null) {
            LibraryLogger.logLoanEvent("DELETE", removed.getBookIsbn(), removed.getPatronId(), "Loan deleted");
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(String loanId) {
        return loans.containsKey(loanId);
    }

    @Override
    public int count() {
        return loans.size();
    }

    @Override
    public void deleteAll() {
        loans.clear();
        LibraryLogger.info(LibraryLogger.LOAN, "All loans deleted from repository");
    }

    /**
     * Finds all loans by patron ID.
     *
     * @param patronId Patron ID
     * @return List of loans for the patron
     */
    public List<LoanRecord> findByPatronId(String patronId) {
        return loans.values().stream()
                .filter(loan -> patronId.equals(loan.getPatronId()))
                .collect(Collectors.toList());
    }

    /**
     * Finds all loans by book ISBN.
     *
     * @param isbn Book ISBN
     * @return List of loans for the book
     */
    public List<LoanRecord> findByBookIsbn(String isbn) {
        return loans.values().stream()
                .filter(loan -> isbn.equals(loan.getBookIsbn()))
                .collect(Collectors.toList());
    }

    /**
     * Finds all active loans.
     *
     * @return List of active loans
     */
    public List<LoanRecord> findActiveLoans() {
        return loans.values().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Finds all overdue loans.
     *
     * @return List of overdue loans
     */
    public List<LoanRecord> findOverdueLoans() {
        return loans.values().stream()
                .filter(LoanRecord::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Finds active loans for a specific patron.
     *
     * @param patronId Patron ID
     * @return List of active loans for the patron
     */
    public List<LoanRecord> findActiveByPatronId(String patronId) {
        return loans.values().stream()
                .filter(loan -> patronId.equals(loan.getPatronId()))
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Finds the active loan for a specific book.
     *
     * @param isbn Book ISBN
     * @return Optional containing the active loan if found
     */
    public Optional<LoanRecord> findActiveByBookIsbn(String isbn) {
        return loans.values().stream()
                .filter(loan -> isbn.equals(loan.getBookIsbn()))
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .findFirst();
    }

    /**
     * Finds all loans by status.
     *
     * @param status Loan status
     * @return List of loans with the status
     */
    public List<LoanRecord> findByStatus(LoanStatus status) {
        return loans.values().stream()
                .filter(loan -> loan.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Gets the borrowing history for a patron (all loans including returned).
     *
     * @param patronId Patron ID
     * @return List of all loans for the patron, sorted by checkout date (newest first)
     */
    public List<LoanRecord> getBorrowingHistory(String patronId) {
        return loans.values().stream()
                .filter(loan -> patronId.equals(loan.getPatronId()))
                .sorted((l1, l2) -> l2.getCheckoutDate().compareTo(l1.getCheckoutDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets count of active loans.
     *
     * @return Number of active loans
     */
    public int countActive() {
        return (int) loans.values().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .count();
    }

    /**
     * Gets count of overdue loans.
     *
     * @return Number of overdue loans
     */
    public int countOverdue() {
        return (int) loans.values().stream()
                .filter(LoanRecord::isOverdue)
                .count();
    }

    /**
     * Gets count of active loans for a patron.
     *
     * @param patronId Patron ID
     * @return Number of active loans for the patron
     */
    public int countActiveByPatron(String patronId) {
        return (int) loans.values().stream()
                .filter(loan -> patronId.equals(loan.getPatronId()))
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .count();
    }
}
