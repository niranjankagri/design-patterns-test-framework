package com.niranjankagri.patterns.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the finished tests and writes a Markdown report when the run ends ({@code report.md}): a summary line and
 * one table row per test. CI appends it to the job summary.
 */
public final class ReportListener implements TestEventListener {

    private final List<TestEvent> finished = new ArrayList<>();
    private String context = "";

    @Override
    public synchronized void onEvent(TestEvent e) {
        switch (e.kind()) {
            case RUN_STARTED -> {
                finished.clear();
                context = e.context();
            }
            case TEST_FINISHED -> finished.add(e);
            case RUN_FINISHED -> write(OutputDir.resolve("report.md"));
            default -> { }
        }
    }

    /** Writes the report of the tests collected so far to this file. */
    public synchronized void write(Path file) {
        long passed = count(TestEvent.Outcome.PASSED);
        long failed = count(TestEvent.Outcome.FAILED);
        long skipped = count(TestEvent.Outcome.SKIPPED);
        StringBuilder md = new StringBuilder("## Test report: ").append(context).append("\n\n")
                .append("%d tests: %d passed, %d failed, %d skipped%n%n".formatted(finished.size(), passed, failed, skipped))
                .append("| Test | Outcome | Time (ms) | Message |\n|---|---|---:|---|\n");
        for (TestEvent e : finished) {
            md.append("| %s | %s | %d | %s |%n".formatted(cell(e.test()), e.outcome(), e.duration().toMillis(),
                    cell(e.message())));
        }
        try {
            Files.writeString(file, md, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private long count(TestEvent.Outcome outcome) {
        return finished.stream().filter(e -> e.outcome() == outcome).count();
    }

    /** Keeps a table cell on one line and its pipes from breaking the table. */
    private static String cell(String text) {
        String oneLine = text.lines().findFirst().orElse("").replace("|", "\\|");
        return oneLine.length() > 160 ? oneLine.substring(0, 157) + "..." : oneLine;
    }
}
