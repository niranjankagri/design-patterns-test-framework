package com.niranjankagri.patterns.events;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Delivers test events to the subscribed listeners (Observer pattern: the subject). The tests and the TestNG bridge
 * only publish; they never call a reporter directly, so reporting can change without touching them.
 *
 * <p>A listener that throws is reported on stderr and skipped for that event: one broken report must not hide the
 * others or fail the test that produced the event.
 */
public final class TestEventBus {

    private static final TestEventBus GLOBAL = withServiceListeners();

    private final List<TestEventListener> listeners = new CopyOnWriteArrayList<>();

    /** The bus of this run, with the listeners from META-INF/services already subscribed. */
    public static TestEventBus global() {
        return GLOBAL;
    }

    /** An empty bus (for unit tests of listeners). */
    public static TestEventBus empty() {
        return new TestEventBus();
    }

    private static TestEventBus withServiceListeners() {
        TestEventBus bus = new TestEventBus();
        ServiceLoader.load(TestEventListener.class).forEach(bus::subscribe);
        return bus;
    }

    /** Adds a listener; it receives every later event. */
    public void subscribe(TestEventListener listener) {
        listeners.add(listener);
    }

    /** Removes a listener. */
    public void unsubscribe(TestEventListener listener) {
        listeners.remove(listener);
    }

    /** The listeners subscribed now. */
    public List<TestEventListener> listeners() {
        return List.copyOf(listeners);
    }

    /** Sends the event to every listener, in subscription order. */
    public void publish(TestEvent event) {
        for (TestEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                System.err.println("Listener " + listener.getClass().getSimpleName() + " failed on " + event.kind()
                        + ": " + e);
            }
        }
    }
}
