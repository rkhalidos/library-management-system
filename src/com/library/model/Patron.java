package com.library.model;

import com.library.observer.LibraryObserver;

import java.util.*;

/**
 * Represents a library patron (member).
 * Implements LibraryObserver to receive notifications about reserved books.
 */
public class Patron implements LibraryObserver {
    private final String patronId;
    private String name;
    private String email;
    private String phone;
    private PatronType patronType;
    private final List<LoanRecord> borrowingHistory;
    private final Set<String> preferences; // genres, favorite authors
    private final List<String> notifications;
    private int currentBorrowedCount;

    /**
     * Constructs a new Patron with the specified attributes.
     *
     * @param patronId   Unique patron identifier
     * @param name       Patron's full name
     * @param email      Patron's email address
     * @param patronType Type of patron (REGULAR, STUDENT, FACULTY, SENIOR)
     */
    public Patron(String patronId, String name, String email, PatronType patronType) {
        this.patronId = patronId;
        this.name = name;
        this.email = email;
        this.patronType = patronType;
        this.borrowingHistory = new ArrayList<>();
        this.preferences = new HashSet<>();
        this.notifications = new ArrayList<>();
        this.currentBorrowedCount = 0;
    }

    /**
     * Constructs a new Patron with default REGULAR type.
     *
     * @param patronId Unique patron identifier
     * @param name     Patron's full name
     * @param email    Patron's email address
     */
    public Patron(String patronId, String name, String email) {
        this(patronId, name, email, PatronType.REGULAR);
    }

    // Getters
    public String getPatronId() {
        return patronId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public PatronType getPatronType() {
        return patronType;
    }

    public List<LoanRecord> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    public Set<String> getPreferences() {
        return Collections.unmodifiableSet(preferences);
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public int getCurrentBorrowedCount() {
        return currentBorrowedCount;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPatronType(PatronType patronType) {
        this.patronType = patronType;
    }

    /**
     * Adds a loan record to the patron's borrowing history.
     *
     * @param record The loan record to add
     */
    public void addLoanRecord(LoanRecord record) {
        borrowingHistory.add(record);
        currentBorrowedCount++;
    }

    /**
     * Marks a book as returned and decrements the borrowed count.
     */
    public void returnBook() {
        if (currentBorrowedCount > 0) {
            currentBorrowedCount--;
        }
    }

    /**
     * Checks if the patron can borrow more books.
     *
     * @return true if the patron can borrow more books
     */
    public boolean canBorrow() {
        return currentBorrowedCount < patronType.getMaxBooks();
    }

    /**
     * Gets the maximum number of books this patron can borrow.
     *
     * @return maximum number of books
     */
    public int getMaxBooks() {
        return patronType.getMaxBooks();
    }

    /**
     * Gets the loan period in days for this patron type.
     *
     * @return loan period in days
     */
    public int getLoanPeriodDays() {
        return patronType.getLoanPeriodDays();
    }

    /**
     * Adds a preference (genre or author) to the patron's preferences.
     *
     * @param preference The preference to add
     */
    public void addPreference(String preference) {
        preferences.add(preference.toLowerCase());
    }

    /**
     * Removes a preference from the patron's preferences.
     *
     * @param preference The preference to remove
     */
    public void removePreference(String preference) {
        preferences.remove(preference.toLowerCase());
    }

    /**
     * Clears all notifications.
     */
    public void clearNotifications() {
        notifications.clear();
    }

    /**
     * Gets the count of unread notifications.
     *
     * @return number of notifications
     */
    public int getNotificationCount() {
        return notifications.size();
    }

    /**
     * Gets all unique genres from borrowing history.
     *
     * @return set of genres borrowed
     */
    public Set<String> getBorrowedGenres() {
        Set<String> genres = new HashSet<>();
        // This would be populated when we have access to book details
        // For now, return preferences as a proxy
        return new HashSet<>(preferences);
    }

    /**
     * Observer pattern implementation - receives notifications.
     *
     * @param eventType Type of event
     * @param message   Notification message
     */
    @Override
    public void update(String eventType, String message) {
        String notification = String.format("[%s] %s", eventType, message);
        notifications.add(notification);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patron patron = (Patron) o;
        return Objects.equals(patronId, patron.patronId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patronId);
    }

    @Override
    public String toString() {
        return String.format("Patron{id='%s', name='%s', email='%s', type=%s, borrowed=%d/%d}",
                patronId, name, email, patronType, currentBorrowedCount, getMaxBooks());
    }
}
