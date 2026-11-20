package com.niranjankagri.patterns.unit;

import com.niranjankagri.patterns.events.LogListener;
import com.niranjankagri.patterns.events.MetricsListener;
import com.niranjankagri.patterns.events.ReportListener;
import com.niranjankagri.patterns.events.ScreenshotListener;
import com.niranjankagri.patterns.events.TestEvent;
import com.niranjankagri.patterns.events.TestEvent.Outcome;
import com.niranjankagri.patterns.events.TestEventBus;
import com.niranjankagri.patterns.events.TestEventListener;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The Observer: subscription, delivery, isolation of a failing listener, and the listeners' outputs. */
public class EventBusTest {

    @Test(description = "TC-701 every subscribed listener receives every event, in order")
    public void delivery() {
        TestEventBus bus = TestEventBus.empty();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        bus.subscribe(e -> first.add(e.kind().name()));
        bus.subscribe(e -> second.add(e.kind().name()));

        bus.publish(TestEvent.runStarted("unit"));
        bus.publish(TestEvent.testFinished("t", "unit", Outcome.PASSED, Duration.ofMillis(5), ""));

        assertThat(first).containsExactly("RUN_STARTED", "TEST_FINISHED");
        assertThat(second).isEqualTo(first);
    }

    @Test(description = "TC-702 a listener that throws does not stop the others")
    public void failingListenerIsIsolated() {
        TestEventBus bus = TestEventBus.empty();
        List<TestEvent> received = new ArrayList<>();
        bus.subscribe(e -> {
            throw new IllegalStateException("broken report");
        });
        bus.subscribe(received::add);

        bus.publish(TestEvent.runStarted("unit"));

        assertThat(received).hasSize(1);
    }

    @Test(description = "TC-703 an unsubscribed listener receives nothing more")
    public void unsubscribe() {
        TestEventBus bus = TestEventBus.empty();
        List<TestEvent> received = new ArrayList<>();
        TestEventListener listener = received::add;
        bus.subscribe(listener);
        bus.publish(TestEvent.runStarted("unit"));
        bus.unsubscribe(listener);
        bus.publish(TestEvent.runStarted("unit"));

        assertThat(received).hasSize(1);
    }

    @Test(description = "TC-704 the global bus subscribes the listeners named in META-INF/services")
    public void serviceListeners() {
        assertThat(TestEventBus.global().listeners()).extracting(Object::getClass)
                .contains(LogListener.class, ReportListener.class, MetricsListener.class, ScreenshotListener.class);
    }

    @Test(description = "TC-705 the report lists each test with a summary line, and escapes table characters")
    public void report() throws IOException {
        ReportListener report = new ReportListener();
        report.onEvent(TestEvent.runStarted("selenium/chrome"));
        report.onEvent(TestEvent.testFinished("TC-1 a", "selenium/chrome", Outcome.PASSED, Duration.ofMillis(12), ""));
        report.onEvent(TestEvent.testFinished("TC-2 b", "selenium/chrome", Outcome.FAILED, Duration.ofMillis(30),
                "AssertionError: expected a|b\nsecond line"));
        Path file = Files.createTempFile("report", ".md");

        report.write(file);

        assertThat(Files.readString(file)).contains("## Test report: selenium/chrome",
                "2 tests: 1 passed, 1 failed, 0 skipped", "| TC-1 a | PASSED | 12 |  |",
                "| TC-2 b | FAILED | 30 | AssertionError: expected a\\|b |").doesNotContain("second line");
    }

    @Test(description = "TC-706 the metrics count outcomes and name the slowest test")
    public void metrics() {
        MetricsListener metrics = new MetricsListener();
        metrics.onEvent(TestEvent.runStarted("x"));
        metrics.onEvent(TestEvent.testFinished("fast", "x", Outcome.PASSED, Duration.ofMillis(10), ""));
        metrics.onEvent(TestEvent.testFinished("slow", "x", Outcome.PASSED, Duration.ofMillis(90), ""));
        metrics.onEvent(TestEvent.testFinished("skipped", "x", Outcome.SKIPPED, Duration.ZERO, ""));

        assertThat(metrics.json("x", Duration.ofSeconds(1))).contains("\"passed\":2", "\"failed\":0", "\"skipped\":1",
                "\"testTimeMs\":100", "\"runTimeMs\":1000", "\"slowestTest\":\"slow\"", "\"slowestMs\":90");
    }
}
