package com.niranjankagri.patterns.browser;

import java.util.List;

/**
 * What page objects and tests may do with a browser, whatever engine drives it (Adapter pattern: the target
 * interface). {@link SeleniumBrowser} and {@link PlaywrightBrowser} translate these calls to their own APIs, and
 * {@link BrowserFactory} decides which one a run gets.
 *
 * <p>Elements are found by their {@code data-testid}: stable for tests, independent of layout and styling, and the
 * same selector works in both engines.
 */
public interface Browser extends AutoCloseable {

    /** Opens a path of the shop, for example {@code /login}. */
    void open(String path);

    /** Replaces the text of an input field. */
    void type(String testId, String text);

    /** Clicks an element that does not load a new page. */
    void click(String testId);

    /** Clicks an element (a form button or link) and waits until the next page has loaded. */
    void clickAndWaitForPage(String testId);

    /** The visible text of an element, waiting until it is visible. */
    String text(String testId);

    /** The visible texts of every element with this test id, in page order (empty when there is none). */
    List<String> texts(String testId);

    /** Whether such an element is visible now (no waiting). */
    boolean isVisible(String testId);

    /** The path of the current page, for example {@code /products}. */
    String currentPath();

    /** A PNG screenshot of the current page. */
    byte[] screenshot();

    /** Engine and browser, for example "selenium/chrome". */
    String name();

    /** Closes the browser and frees its resources. */
    @Override
    void close();
}
