package com.niranjankagri.patterns.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;

/**
 * The run configuration: which engine and browser, local or remote, where the shop is, and which test-data source.
 *
 * <p>Values come from system properties ({@code -Dengine=playwright}), then {@code framework.properties} on the
 * classpath, then the defaults below. Everything that decides how a run behaves is read here once, so the Factory and
 * Strategy selections are config-driven rather than spread through the tests.
 *
 * @param engine     selenium or playwright
 * @param browser    chrome, firefox or edge (Selenium); chromium, firefox or webkit (Playwright)
 * @param headless   run without a visible window
 * @param remote     Selenium only: use a Selenium Grid at {@code gridUrl} instead of a local driver
 * @param gridUrl    the Grid's address, used when {@code remote} is true
 * @param appUrl     the Pattern Shop to test (a local container, the CI service container or the hosted demo)
 * @param dataSource where test data comes from: json, csv or api
 * @param timeout    how long an element may take to appear
 */
public record TestConfig(Engine engine, String browser, boolean headless, boolean remote, URI gridUrl, URI appUrl,
                         String dataSource, Duration timeout) {

    /** The two browser engines behind the Browser interface. */
    public enum Engine { SELENIUM, PLAYWRIGHT }

    /** Reads the configuration of this run. */
    public static TestConfig load() {
        Properties file = new Properties();
        try (InputStream in = TestConfig.class.getResourceAsStream("/framework.properties")) {
            if (in != null) {
                file.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("framework.properties could not be read", e);
        }
        Engine engine = Engine.valueOf(value(file, "engine", "selenium").toUpperCase(Locale.ROOT));
        String defaultBrowser = engine == Engine.SELENIUM ? "chrome" : "chromium";
        return new TestConfig(
                engine,
                value(file, "browser", defaultBrowser).toLowerCase(Locale.ROOT),
                Boolean.parseBoolean(value(file, "headless", "true")),
                Boolean.parseBoolean(value(file, "remote", "false")),
                URI.create(value(file, "grid.url", "http://localhost:4444")),
                URI.create(stripSlash(value(file, "app.url", "http://localhost:8080"))),
                value(file, "data.source", "json").toLowerCase(Locale.ROOT),
                Duration.ofSeconds(Long.parseLong(value(file, "timeout.seconds", "10"))));
    }

    /** A short label for reports, for example "selenium/chrome (remote)". */
    public String label() {
        return engine.name().toLowerCase(Locale.ROOT) + "/" + browser + (remote ? " (remote)" : "");
    }

    private static String value(Properties file, String key, String fallback) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem.trim();
        }
        return file.getProperty(key, fallback).trim();
    }

    private static String stripSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
