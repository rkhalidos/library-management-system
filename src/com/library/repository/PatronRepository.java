package com.library.repository;

import com.library.model.Patron;
import com.library.model.PatronType;
import com.library.util.LibraryLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository implementation for Patron entities.
 * Provides in-memory storage with thread-safe operations.
 */
public class PatronRepository implements Repository<Patron, String> {
    private final Map<String, Patron> patrons;

    public PatronRepository() {
        this.patrons = new ConcurrentHashMap<>();
    }

    @Override
    public Patron save(Patron patron) {
        patrons.put(patron.getPatronId(), patron);
        LibraryLogger.logPatronEvent("SAVE", patron.getPatronId(), patron.getName());
        return patron;
    }

    @Override
    public Optional<Patron> findById(String patronId) {
        return Optional.ofNullable(patrons.get(patronId));
    }

    @Override
    public List<Patron> findAll() {
        return new ArrayList<>(patrons.values());
    }

    @Override
    public boolean deleteById(String patronId) {
        Patron removed = patrons.remove(patronId);
        if (removed != null) {
            LibraryLogger.logPatronEvent("DELETE", patronId, removed.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(String patronId) {
        return patrons.containsKey(patronId);
    }

    @Override
    public int count() {
        return patrons.size();
    }

    @Override
    public void deleteAll() {
        patrons.clear();
        LibraryLogger.info(LibraryLogger.PATRON, "All patrons deleted from repository");
    }

    /**
     * Finds a patron by email.
     *
     * @param email Email to search for
     * @return Optional containing the patron if found
     */
    public Optional<Patron> findByEmail(String email) {
        return patrons.values().stream()
                .filter(patron -> email.equalsIgnoreCase(patron.getEmail()))
                .findFirst();
    }

    /**
     * Finds patrons by name (partial match).
     *
     * @param name Name to search for
     * @return List of matching patrons
     */
    public List<Patron> findByName(String name) {
        return patrons.values().stream()
                .filter(patron -> patron.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Finds all patrons by type.
     *
     * @param patronType Patron type to filter by
     * @return List of patrons with the given type
     */
    public List<Patron> findByType(PatronType patronType) {
        return patrons.values().stream()
                .filter(patron -> patron.getPatronType() == patronType)
                .collect(Collectors.toList());
    }

    /**
     * Finds patrons with active loans (currently borrowing books).
     *
     * @return List of patrons with active loans
     */
    public List<Patron> findWithActiveLoans() {
        return patrons.values().stream()
                .filter(patron -> patron.getCurrentBorrowedCount() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Finds patrons who can still borrow books.
     *
     * @return List of patrons who can borrow
     */
    public List<Patron> findCanBorrow() {
        return patrons.values().stream()
                .filter(Patron::canBorrow)
                .collect(Collectors.toList());
    }

    /**
     * Finds patrons with unread notifications.
     *
     * @return List of patrons with notifications
     */
    public List<Patron> findWithNotifications() {
        return patrons.values().stream()
                .filter(patron -> patron.getNotificationCount() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Finds patrons by preference.
     *
     * @param preference Preference (genre/author) to search for
     * @return List of patrons with the preference
     */
    public List<Patron> findByPreference(String preference) {
        return patrons.values().stream()
                .filter(patron -> patron.getPreferences().contains(preference.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets count of patrons by type.
     *
     * @param patronType Patron type
     * @return Number of patrons of the type
     */
    public int countByType(PatronType patronType) {
        return (int) patrons.values().stream()
                .filter(patron -> patron.getPatronType() == patronType)
                .count();
    }
}
