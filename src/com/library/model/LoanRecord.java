package com.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a loan record for a book borrowed by a patron.
 * Tracks checkout date, due date, return date, and loan status.
 */
public class LoanRecord {
    private final String loanId;
    private final String bookIsbn;
    private final String patronId;
    private final LocalDate checkoutDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    /**
     * Constructs a new LoanRecord with default loan period.
     *
     * @param bookIsbn  ISBN of the borrowed book
     * @param patronId  ID of the patron borrowing the book
     */
    public LoanRecord(String bookIsbn, String patronId) {
        this.loanId = UUID.randomUUID().toString();
        this.bookIsbn = bookIsbn;
        this.patronId = patronId;
        this.checkoutDate = LocalDate.now();
        this.dueDate = checkoutDate.plusDays(DEFAULT_LOAN_PERIOD_DAYS);
        this.status = LoanStatus.ACTIVE;
        this.returnDate = null;
    }

    /**
     * Constructs a new LoanRecord with custom loan period.
     *
     * @param bookIsbn       ISBN of the borrowed book
     * @param patronId       ID of the patron borrowing the book
     * @param loanPeriodDays Number of days for the loan period
     */
    public LoanRecord(String bookIsbn, String patronId, int loanPeriodDays) {
        this.loanId = UUID.randomUUID().toString();
        this.bookIsbn = bookIsbn;
        this.patronId = patronId;
        this.checkoutDate = LocalDate.now();
        this.dueDate = checkoutDate.plusDays(loanPeriodDays);
        this.status = LoanStatus.ACTIVE;
        this.returnDate = null;
    }

    // Getters
    public String getLoanId() {
        return loanId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public String getPatronId() {
        return patronId;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    /**
     * Marks the loan as returned and sets the return date.
     */
    public void markReturned() {
        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
    }

    /**
     * Checks if the loan is overdue and updates status accordingly.
     *
     * @return true if the loan is overdue, false otherwise
     */
    public boolean isOverdue() {
        if (status == LoanStatus.RETURNED) {
            return false;
        }
        if (LocalDate.now().isAfter(dueDate)) {
            this.status = LoanStatus.OVERDUE;
            return true;
        }
        return false;
    }

    /**
     * Gets the number of days until due date (negative if overdue).
     *
     * @return days until due date
     */
    public long getDaysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Gets the number of days the book was/has been borrowed.
     *
     * @return number of days borrowed
     */
    public long getDaysBorrowed() {
        LocalDate endDate = (returnDate != null) ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(checkoutDate, endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanRecord that = (LoanRecord) o;
        return Objects.equals(loanId, that.loanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }

    @Override
    public String toString() {
        return String.format("LoanRecord{loanId='%s', bookIsbn='%s', patronId='%s', checkout=%s, due=%s, return=%s, status=%s}",
                loanId, bookIsbn, patronId, checkoutDate, dueDate, returnDate, status);
    }
}
