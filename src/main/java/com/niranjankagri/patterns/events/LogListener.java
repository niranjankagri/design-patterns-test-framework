package com.niranjankagri.patterns.events;

import java.lang.System.Logger.Level;

/** Writes one log line per finished test, and the start and end of the run. */
public final class LogListener implements TestEventListener {

    private static final System.Logger LOG = System.getLogger("test-events");

    @Override
    public void onEvent(TestEvent e) {
        switch (e.kind()) {
            case RUN_STARTED -> LOG.log(Level.INFO, "Run started on {0}", e.context());
            case TEST_FINISHED -> LOG.log(e.outcome() == TestEvent.Outcome.FAILED ? Level.WARNING : Level.INFO,
                    "{0} {1} ({2} ms){3}", e.outcome(), e.test(), e.duration().toMillis(),
                    e.message().isEmpty() ? "" : ": " + e.message());
            case RUN_FINISHED -> LOG.log(Level.INFO, "Run finished in {0} s", e.duration().toSeconds());
            default -> { }
        }
    }
}
