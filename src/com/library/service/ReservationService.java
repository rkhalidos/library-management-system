package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.PatronNotFoundException;
import com.library.model.*;
import com.library.observer.NotificationService;
import com.library.repository.BookRepository;
import com.library.repository.PatronRepository;
import com.library.util.LibraryLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service class for book reservation operations.
 * Handles reserving books and notifying patrons when reserved books become available.
 */
public class ReservationService {
    private final Map<String, Reservation> reservations; // reservationId -> Reservation
    private final Map<String, Queue<Reservation>> bookReservationQueues; // isbn -> Queue of reservations
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final NotificationService notificationService;

    public ReservationService(BookRepository bookRepository, PatronRepository patronRepository) {
        this.reservations = new ConcurrentHashMap<>();
        this.bookReservationQueues = new ConcurrentHashMap<>();
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.notificationService = NotificationService.getInstance();
    }

    /**
     * Creates a reservation for a book.
     *
     * @param isbn     ISBN of the book to reserve
     * @param patronId ID of the patron making the reservation
     * @return The created reservation
     * @throws BookNotFoundException   if book not found
     * @throws PatronNotFoundException if patron not found
     */
    public Reservation reserveBook(String isbn, String patronId) {
        // Validate book exists
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        
        // Validate patron exists
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new PatronNotFoundException(patronId));
        
        // Check if book is available (no need to reserve)
        if (book.isAvailable()) {
            throw new IllegalStateException("Book is available, no need to reserve. Please checkout instead.");
        }
        
        // Check if patron already has a reservation for this book
        if (hasActiveReservation(isbn, patronId)) {
            throw new IllegalStateException("Patron already has an active reservation for this book");
        }
        
        // Create reservation
        Reservation reservation = new Reservation(isbn, patronId);
        reservations.put(reservation.getReservationId(), reservation);
        
        // Add to queue
        bookReservationQueues.computeIfAbsent(isbn, k -> new LinkedList<>()).add(reservation);
        
        LibraryLogger.logReservationEvent("CREATE", isbn, patronId, 
            "Queue position: " + getQueuePosition(isbn, patronId));
        
        return reservation;
    }

    /**
     * Cancels a reservation.
     *
     * @param reservationId ID of the reservation to cancel
     * @return true if cancelled successfully
     */
    public boolean cancelReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            return false;
        }
        
        if (!reservation.isActive()) {
            throw new IllegalStateException("Cannot cancel a non-active reservation");
        }
        
        reservation.cancel();
        
        // Remove from queue
        Queue<Reservation> queue = bookReservationQueues.get(reservation.getBookIsbn());
        if (queue != null) {
            queue.removeIf(r -> r.getReservationId().equals(reservationId));
        }
        
        LibraryLogger.logReservationEvent("CANCEL", reservation.getBookIsbn(), 
            reservation.getPatronId(), "Reservation cancelled");
        
        return true;
    }

    /**
     * Gets the next pending reservation for a book.
     *
     * @param isbn Book ISBN
     * @return Optional containing the next reservation if found
     */
    public Optional<Reservation> getNextPendingReservation(String isbn) {
        Queue<Reservation> queue = bookReservationQueues.get(isbn);
        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }
        
        // Find the first pending reservation
        for (Reservation reservation : queue) {
            if (reservation.isPending()) {
                return Optional.of(reservation);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Processes a reservation when the book becomes available.
     * Marks the reservation as ready and notifies the patron.
     *
     * @param reservationId ID of the reservation
     */
    public void processReservationReady(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null || !reservation.isPending()) {
            return;
        }
        
        reservation.markReady();
        
        // Notify the patron
        patronRepository.findById(reservation.getPatronId()).ifPresent(patron -> {
            bookRepository.findById(reservation.getBookIsbn()).ifPresent(book -> {
                notificationService.notifyBookAvailable(patron, book.getTitle(), book.getIsbn());
            });
        });
        
        LibraryLogger.logReservationEvent("READY", reservation.getBookIsbn(), 
            reservation.getPatronId(), "Patron notified");
    }

    /**
     * Fulfills a reservation (patron picked up the book).
     *
     * @param reservationId ID of the reservation
     * @return true if fulfilled successfully
     */
    public boolean fulfillReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            return false;
        }
        
        if (!reservation.isReady()) {
            throw new IllegalStateException("Reservation is not ready for fulfillment");
        }
        
        reservation.markFulfilled();
        
        // Remove from queue
        Queue<Reservation> queue = bookReservationQueues.get(reservation.getBookIsbn());
        if (queue != null) {
            queue.removeIf(r -> r.getReservationId().equals(reservationId));
        }
        
        LibraryLogger.logReservationEvent("FULFILL", reservation.getBookIsbn(), 
            reservation.getPatronId(), "Reservation fulfilled");
        
        return true;
    }

    /**
     * Expires a reservation (patron didn't pick up in time).
     *
     * @param reservationId ID of the reservation
     * @return true if expired successfully
     */
    public boolean expireReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            return false;
        }
        
        reservation.expire();
        
        // Remove from queue
        Queue<Reservation> queue = bookReservationQueues.get(reservation.getBookIsbn());
        if (queue != null) {
            queue.removeIf(r -> r.getReservationId().equals(reservationId));
        }
        
        LibraryLogger.logReservationEvent("EXPIRE", reservation.getBookIsbn(), 
            reservation.getPatronId(), "Reservation expired");
        
        return true;
    }

    /**
     * Gets a reservation by ID.
     *
     * @param reservationId Reservation ID
     * @return Optional containing the reservation if found
     */
    public Optional<Reservation> getReservationById(String reservationId) {
        return Optional.ofNullable(reservations.get(reservationId));
    }

    /**
     * Gets all reservations for a patron.
     *
     * @param patronId Patron ID
     * @return List of reservations
     */
    public List<Reservation> getPatronReservations(String patronId) {
        return reservations.values().stream()
                .filter(r -> r.getPatronId().equals(patronId))
                .collect(Collectors.toList());
    }

    /**
     * Gets active reservations for a patron.
     *
     * @param patronId Patron ID
     * @return List of active reservations
     */
    public List<Reservation> getPatronActiveReservations(String patronId) {
        return reservations.values().stream()
                .filter(r -> r.getPatronId().equals(patronId))
                .filter(Reservation::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Gets all reservations for a book.
     *
     * @param isbn Book ISBN
     * @return List of reservations
     */
    public List<Reservation> getBookReservations(String isbn) {
        Queue<Reservation> queue = bookReservationQueues.get(isbn);
        if (queue == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(queue);
    }

    /**
     * Gets the queue position for a patron's reservation on a book.
     *
     * @param isbn     Book ISBN
     * @param patronId Patron ID
     * @return Queue position (1-based), or -1 if not found
     */
    public int getQueuePosition(String isbn, String patronId) {
        Queue<Reservation> queue = bookReservationQueues.get(isbn);
        if (queue == null) {
            return -1;
        }
        
        int position = 1;
        for (Reservation reservation : queue) {
            if (reservation.getPatronId().equals(patronId) && reservation.isActive()) {
                return position;
            }
            if (reservation.isActive()) {
                position++;
            }
        }
        
        return -1;
    }

    /**
     * Checks if a patron has an active reservation for a book.
     *
     * @param isbn     Book ISBN
     * @param patronId Patron ID
     * @return true if patron has active reservation
     */
    public boolean hasActiveReservation(String isbn, String patronId) {
        return reservations.values().stream()
                .anyMatch(r -> r.getBookIsbn().equals(isbn) && 
                              r.getPatronId().equals(patronId) && 
                              r.isActive());
    }

    /**
     * Gets count of active reservations for a book.
     *
     * @param isbn Book ISBN
     * @return Number of active reservations
     */
    public int getReservationCount(String isbn) {
        Queue<Reservation> queue = bookReservationQueues.get(isbn);
        if (queue == null) {
            return 0;
        }
        return (int) queue.stream().filter(Reservation::isActive).count();
    }

    /**
     * Gets total reservation count.
     *
     * @return Total number of reservations
     */
    public int getTotalReservationCount() {
        return reservations.size();
    }

    /**
     * Gets count of active reservations.
     *
     * @return Number of active reservations
     */
    public int getActiveReservationCount() {
        return (int) reservations.values().stream()
                .filter(Reservation::isActive)
                .count();
    }

    /**
     * Gets all reservations that are ready for pickup.
     *
     * @return List of ready reservations
     */
    public List<Reservation> getReadyReservations() {
        return reservations.values().stream()
                .filter(Reservation::isReady)
                .collect(Collectors.toList());
    }
}
