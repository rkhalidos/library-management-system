package com.library.observer;

/**
 * Observer interface for the Observer design pattern.
 * Implemented by classes that need to be notified of library events.
 */
public interface LibraryObserver {
    /**
     * Called when the observer needs to be notified of an event.
     *
     * @param eventType Type of event that occurred
     * @param message   Detailed message about the event
     */
    void update(String eventType, String message);
}
