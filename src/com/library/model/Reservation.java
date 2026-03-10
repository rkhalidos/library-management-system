package com.library.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a book reservation made by a patron.
 * Used when a book is currently checked out and a patron wants to be notified when it becomes available.
 */
public class Reservation {
    private final String reservationId;
    private final String bookIsbn;
    private final String patronId;
    private final LocalDateTime reservationDate;
    private ReservationStatus status;
    private LocalDateTime notificationDate;

    /**
     * Enumeration for reservation status.
     */
    public enum ReservationStatus {
        PENDING,    // Waiting for book to be returned
        READY,      // Book is available for pickup
        FULFILLED,  // Patron has picked up the book
        CANCELLED,  // Reservation was cancelled
        EXPIRED     // Reservation expired (patron didn't pick up in time)
    }

    /**
     * Constructs a new Reservation.
     *
     * @param bookIsbn ISBN of the reserved book
     * @param patronId ID of the patron making the reservation
     */
    public Reservation(String bookIsbn, String patronId) {
        this.reservationId = UUID.randomUUID().toString();
        this.bookIsbn = bookIsbn;
        this.patronId = patronId;
        this.reservationDate = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.notificationDate = null;
    }

    // Getters
    public String getReservationId() {
        return reservationId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public String getPatronId() {
        return patronId;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getNotificationDate() {
        return notificationDate;
    }

    /**
     * Marks the reservation as ready (book is now available).
     */
    public void markReady() {
        this.status = ReservationStatus.READY;
        this.notificationDate = LocalDateTime.now();
    }

    /**
     * Marks the reservation as fulfilled (patron picked up the book).
     */
    public void markFulfilled() {
        this.status = ReservationStatus.FULFILLED;
    }

    /**
     * Cancels the reservation.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    /**
     * Marks the reservation as expired.
     */
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }

    /**
     * Checks if the reservation is still active (pending or ready).
     *
     * @return true if reservation is active
     */
    public boolean isActive() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.READY;
    }

    /**
     * Checks if the reservation is pending.
     *
     * @return true if reservation is pending
     */
    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }

    /**
     * Checks if the book is ready for pickup.
     *
     * @return true if book is ready
     */
    public boolean isReady() {
        return status == ReservationStatus.READY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    @Override
    public String toString() {
        return String.format("Reservation{id='%s', bookIsbn='%s', patronId='%s', date=%s, status=%s}",
                reservationId, bookIsbn, patronId, reservationDate, status);
    }
}
