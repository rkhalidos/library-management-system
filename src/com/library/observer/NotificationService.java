package com.library.observer;

import com.library.util.LibraryLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notification service implementing the Observer pattern.
 * Manages subscriptions and notifications for library events.
 */
public class NotificationService implements LibrarySubject {
    private static NotificationService instance;
    private final Map<String, Set<LibraryObserver>> topicSubscribers;
    private final Set<LibraryObserver> globalObservers;

    // Event types
    public static final String EVENT_BOOK_AVAILABLE = "BOOK_AVAILABLE";
    public static final String EVENT_BOOK_DUE_SOON = "BOOK_DUE_SOON";
    public static final String EVENT_BOOK_OVERDUE = "BOOK_OVERDUE";
    public static final String EVENT_RESERVATION_READY = "RESERVATION_READY";
    public static final String EVENT_RESERVATION_EXPIRED = "RESERVATION_EXPIRED";
    public static final String EVENT_NEW_BOOK_ADDED = "NEW_BOOK_ADDED";

    /**
     * Private constructor for singleton pattern.
     */
    private NotificationService() {
        this.topicSubscribers = new ConcurrentHashMap<>();
        this.globalObservers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /**
     * Gets the singleton instance of NotificationService.
     *
     * @return The NotificationService instance
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Attaches a global observer that receives all notifications.
     *
     * @param observer The observer to attach
     */
    @Override
    public void attach(LibraryObserver observer) {
        globalObservers.add(observer);
        LibraryLogger.debug(LibraryLogger.SYSTEM, "Observer attached globally: " + observer);
    }

    /**
     * Detaches a global observer.
     *
     * @param observer The observer to detach
     */
    @Override
    public void detach(LibraryObserver observer) {
        globalObservers.remove(observer);
        // Also remove from all topic subscriptions
        for (Set<LibraryObserver> subscribers : topicSubscribers.values()) {
            subscribers.remove(observer);
        }
        LibraryLogger.debug(LibraryLogger.SYSTEM, "Observer detached: " + observer);
    }

    /**
     * Notifies all global observers of an event.
     *
     * @param eventType Type of event
     * @param message   Event message
     */
    @Override
    public void notifyObservers(String eventType, String message) {
        // Notify global observers
        for (LibraryObserver observer : globalObservers) {
            observer.update(eventType, message);
        }

        // Notify topic-specific subscribers
        Set<LibraryObserver> subscribers = topicSubscribers.get(eventType);
        if (subscribers != null) {
            for (LibraryObserver observer : subscribers) {
                if (!globalObservers.contains(observer)) {
                    observer.update(eventType, message);
                }
            }
        }

        LibraryLogger.info(LibraryLogger.SYSTEM, 
            String.format("Notification sent - Event: %s, Message: %s", eventType, message));
    }

    /**
     * Subscribes an observer to a specific event type/topic.
     *
     * @param eventType The event type to subscribe to
     * @param observer  The observer to subscribe
     */
    public void subscribe(String eventType, LibraryObserver observer) {
        topicSubscribers.computeIfAbsent(eventType, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                       .add(observer);
        LibraryLogger.debug(LibraryLogger.SYSTEM, 
            String.format("Observer subscribed to %s: %s", eventType, observer));
    }

    /**
     * Unsubscribes an observer from a specific event type/topic.
     *
     * @param eventType The event type to unsubscribe from
     * @param observer  The observer to unsubscribe
     */
    public void unsubscribe(String eventType, LibraryObserver observer) {
        Set<LibraryObserver> subscribers = topicSubscribers.get(eventType);
        if (subscribers != null) {
            subscribers.remove(observer);
        }
    }

    /**
     * Notifies a specific observer directly.
     *
     * @param observer  The observer to notify
     * @param eventType Type of event
     * @param message   Event message
     */
    public void notifySpecificObserver(LibraryObserver observer, String eventType, String message) {
        observer.update(eventType, message);
        LibraryLogger.info(LibraryLogger.SYSTEM, 
            String.format("Direct notification sent to %s - Event: %s", observer, eventType));
    }

    /**
     * Sends a book available notification for reserved books.
     *
     * @param observer The patron to notify
     * @param bookTitle Title of the available book
     * @param isbn ISBN of the book
     */
    public void notifyBookAvailable(LibraryObserver observer, String bookTitle, String isbn) {
        String message = String.format("The book '%s' (ISBN: %s) you reserved is now available for pickup!", 
                                       bookTitle, isbn);
        notifySpecificObserver(observer, EVENT_RESERVATION_READY, message);
    }

    /**
     * Sends a due date reminder notification.
     *
     * @param observer The patron to notify
     * @param bookTitle Title of the book
     * @param daysUntilDue Days until the book is due
     */
    public void notifyDueDateReminder(LibraryObserver observer, String bookTitle, long daysUntilDue) {
        String message = String.format("Reminder: '%s' is due in %d day(s). Please return it on time.", 
                                       bookTitle, daysUntilDue);
        notifySpecificObserver(observer, EVENT_BOOK_DUE_SOON, message);
    }

    /**
     * Sends an overdue notification.
     *
     * @param observer The patron to notify
     * @param bookTitle Title of the book
     * @param daysOverdue Days the book is overdue
     */
    public void notifyOverdue(LibraryObserver observer, String bookTitle, long daysOverdue) {
        String message = String.format("OVERDUE: '%s' is %d day(s) overdue. Please return it immediately.", 
                                       bookTitle, daysOverdue);
        notifySpecificObserver(observer, EVENT_BOOK_OVERDUE, message);
    }

    /**
     * Broadcasts a new book addition to all subscribers.
     *
     * @param bookTitle Title of the new book
     * @param author Author of the book
     * @param genre Genre of the book
     */
    public void broadcastNewBook(String bookTitle, String author, String genre) {
        String message = String.format("New book added: '%s' by %s (Genre: %s)", bookTitle, author, genre);
        notifyObservers(EVENT_NEW_BOOK_ADDED, message);
    }

    /**
     * Gets the count of global observers.
     *
     * @return Number of global observers
     */
    public int getGlobalObserverCount() {
        return globalObservers.size();
    }

    /**
     * Gets the count of subscribers for a specific topic.
     *
     * @param eventType The event type/topic
     * @return Number of subscribers
     */
    public int getTopicSubscriberCount(String eventType) {
        Set<LibraryObserver> subscribers = topicSubscribers.get(eventType);
        return subscribers != null ? subscribers.size() : 0;
    }

    /**
     * Clears all observers and subscriptions.
     * Useful for testing.
     */
    public void clearAll() {
        globalObservers.clear();
        topicSubscribers.clear();
    }
}
