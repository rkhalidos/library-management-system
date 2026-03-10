package com.library.model;

/**
 * Enumeration representing different types of library patrons.
 * Different patron types may have different borrowing privileges.
 */
public enum PatronType {
    REGULAR(5, 14),      // 5 books, 14 days loan period
    STUDENT(7, 21),      // 7 books, 21 days loan period
    FACULTY(10, 30),     // 10 books, 30 days loan period
    SENIOR(5, 21);       // 5 books, 21 days loan period

    private final int maxBooks;
    private final int loanPeriodDays;

    PatronType(int maxBooks, int loanPeriodDays) {
        this.maxBooks = maxBooks;
        this.loanPeriodDays = loanPeriodDays;
    }

    public int getMaxBooks() {
        return maxBooks;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }
}
