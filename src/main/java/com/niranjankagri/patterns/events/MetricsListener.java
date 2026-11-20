package com.niranjankagri.patterns.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/**
 * Counts outcomes and test time, and writes them as {@code metrics.json} when the run ends, for a dashboard or a
 * trend over runs (one small JSON object per run).
 */
public final class MetricsListener implements TestEventListener {

    private final Map<TestEvent.Outcome, Integer> counts = new EnumMap<>(TestEvent.Outcome.class);
    private Duration testTime = Duration.ZERO;
    private Duration slowest = Duration.ZERO;
    private String slowestTest = "";

    @Override
    public synchronized void onEvent(TestEvent e) {
        switch (e.kind()) {
            case RUN_STARTED -> {
                counts.clear();
                testTime = Duration.ZERO;
                slowest = Duration.ZERO;
                slowestTest = "";
            }
            case TEST_FINISHED -> {
                counts.merge(e.outcome(), 1, Integer::sum);
                testTime = testTime.plus(e.duration());
                if (e.duration().compareTo(slowest) > 0) {
                    slowest = e.duration();
                    slowestTest = e.test();
                }
            }
            case RUN_FINISHED -> write(e);
            default -> { }
        }
    }

    /** The metrics so far as JSON. */
    public synchronized String json(String context, Duration runTime) {
        return """
                {"context":"%s","passed":%d,"failed":%d,"skipped":%d,"testTimeMs":%d,"runTimeMs":%d,\
                "slowestTest":"%s","slowestMs":%d}
                """.formatted(escape(context), count(TestEvent.Outcome.PASSED), count(TestEvent.Outcome.FAILED),
                count(TestEvent.Outcome.SKIPPED), testTime.toMillis(), runTime.toMillis(), escape(slowestTest),
                slowest.toMillis());
    }

    private void write(TestEvent end) {
        try {
            Files.writeString(OutputDir.resolve("metrics.json"), json(end.context(), end.duration()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private int count(TestEvent.Outcome outcome) {
        return counts.getOrDefault(outcome, 0);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
