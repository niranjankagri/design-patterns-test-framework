package com.niranjankagri.patterns.events;

import java.time.Duration;
import java.time.Instant;

/**
 * Something that happened during the run, sent to every subscribed listener (Observer pattern: the message).
 *
 * @param kind       what happened
 * @param test       the test's name, for example "TC-101 an active user signs in [standard_user]" (empty for run events)
 * @param context    where it ran, for example "selenium/chrome"
 * @param outcome    the result (TEST_FINISHED only, else null)
 * @param duration   how long the test or run took (finished events only, else zero)
 * @param message    the failure message, or a note
 * @param attachment PNG bytes of a screenshot (SCREENSHOT only), else empty
 * @param at         when it happened
 */
public record TestEvent(Kind kind, String test, String context, Outcome outcome, Duration duration, String message,
                        byte[] attachment, Instant at) {

    /** What happened. */
    public enum Kind { RUN_STARTED, TEST_STARTED, TEST_FINISHED, SCREENSHOT, RUN_FINISHED }

    /** How a test ended. */
    public enum Outcome { PASSED, FAILED, SKIPPED }

    /** The run started. */
    public static TestEvent runStarted(String context) {
        return new TestEvent(Kind.RUN_STARTED, "", context, null, Duration.ZERO, "", new byte[0], Instant.now());
    }

    /** A test started. */
    public static TestEvent testStarted(String test, String context) {
        return new TestEvent(Kind.TEST_STARTED, test, context, null, Duration.ZERO, "", new byte[0], Instant.now());
    }

    /** A test finished. */
    public static TestEvent testFinished(String test, String context, Outcome outcome, Duration duration, String message) {
        return new TestEvent(Kind.TEST_FINISHED, test, context, outcome, duration, message, new byte[0], Instant.now());
    }

    /** A screenshot was taken (after a failure). */
    public static TestEvent screenshot(String test, String context, byte[] png) {
        return new TestEvent(Kind.SCREENSHOT, test, context, null, Duration.ZERO, "", png, Instant.now());
    }

    /** The run finished. */
    public static TestEvent runFinished(String context, Duration duration) {
        return new TestEvent(Kind.RUN_FINISHED, "", context, null, duration, "", new byte[0], Instant.now());
    }
}
