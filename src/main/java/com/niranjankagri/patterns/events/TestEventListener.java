package com.niranjankagri.patterns.events;

/**
 * Receives test events (Observer pattern: the observer). Reporting, logging, metrics and screenshots are separate
 * listeners; a new output (for example a chat notification) is one more class, and no test changes.
 *
 * <p>Listeners named in {@code META-INF/services/com.niranjankagri.patterns.events.TestEventListener} are subscribed
 * automatically at the start of the run (Java's ServiceLoader), so a project adds a listener without code changes here.
 */
@FunctionalInterface
public interface TestEventListener {

    /** Called for every event, in the order they happen. */
    void onEvent(TestEvent event);
}
