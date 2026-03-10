package com.library.observer;

/**
 * Subject interface for the Observer design pattern.
 * Implemented by classes that need to notify observers of events.
 */
public interface LibrarySubject {
    /**
     * Attaches an observer to receive notifications.
     *
     * @param observer The observer to attach
     */
    void attach(LibraryObserver observer);

    /**
     * Detaches an observer from receiving notifications.
     *
     * @param observer The observer to detach
     */
    void detach(LibraryObserver observer);

    /**
     * Notifies all attached observers of an event.
     *
     * @param eventType Type of event
     * @param message   Event message
     */
    void notifyObservers(String eventType, String message);
}
