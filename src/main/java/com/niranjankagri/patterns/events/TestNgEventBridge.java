package com.niranjankagri.patterns.events;

import com.niranjankagri.patterns.config.TestConfig;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Turns TestNG's callbacks into {@link TestEvent}s on the global {@link TestEventBus}. This is the only class that
 * knows TestNG's listener API; the listeners only know test events, so they would work unchanged under JUnit.
 * Registered in the suite files.
 */
public final class TestNgEventBridge implements ISuiteListener, ITestListener {

    private final TestEventBus bus = TestEventBus.global();
    private final String context = TestConfig.load().label();
    private Instant started = Instant.now();

    @Override
    public void onStart(ISuite suite) {
        started = Instant.now();
        bus.publish(TestEvent.runStarted(context));
    }

    @Override
    public void onFinish(ISuite suite) {
        bus.publish(TestEvent.runFinished(context, Duration.between(started, Instant.now())));
    }

    @Override
    public void onTestStart(ITestResult result) {
        bus.publish(TestEvent.testStarted(name(result), context));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        finished(result, TestEvent.Outcome.PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        finished(result, TestEvent.Outcome.FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        finished(result, TestEvent.Outcome.SKIPPED);
    }

    private void finished(ITestResult result, TestEvent.Outcome outcome) {
        Throwable error = result.getThrowable();
        String message = error == null ? "" : error.getClass().getSimpleName() + ": " + error.getMessage();
        bus.publish(TestEvent.testFinished(name(result), context, outcome,
                Duration.ofMillis(Math.max(0, result.getEndMillis() - result.getStartMillis())), message));
    }

    /** The test's description (it starts with the test case id) plus its data, for example "[standard_user]". */
    public static String name(ITestResult result) {
        String description = result.getMethod().getDescription();
        String base = description == null || description.isBlank()
                ? result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName()
                : description;
        Object[] parameters = result.getParameters();
        return parameters.length == 0 ? base
                : base + " [" + Arrays.stream(parameters).map(TestNgEventBridge::label).collect(Collectors.joining(", ")) + "]";
    }

    private static String label(Object parameter) {
        if (parameter instanceof com.niranjankagri.patterns.model.User u) {
            return u.username();
        }
        if (parameter instanceof com.niranjankagri.patterns.clients.CatalogClient c) {
            return c.name();
        }
        if (parameter instanceof com.niranjankagri.patterns.data.TestDataSource s) {
            return s.name();
        }
        return String.valueOf(parameter);
    }
}
