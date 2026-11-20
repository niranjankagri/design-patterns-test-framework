package com.niranjankagri.patterns.browser;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import java.net.URI;
import java.util.List;

/**
 * Adapter from the {@link Browser} interface to Playwright for Java. Playwright waits for elements and for the
 * navigation a click starts by itself, so this adapter is mostly a translation of names; it also owns the
 * Playwright process and closes it with the browser.
 */
public final class PlaywrightBrowser implements Browser {

    private final Playwright playwright;
    private final com.microsoft.playwright.Browser browser;
    private final Page page;
    private final URI appUrl;
    private final String name;

    PlaywrightBrowser(Playwright playwright, com.microsoft.playwright.Browser browser, URI appUrl, double timeoutMs,
                      String name) {
        this.playwright = playwright;
        this.browser = browser;
        this.page = browser.newContext().newPage();
        this.page.setDefaultTimeout(timeoutMs);
        this.appUrl = appUrl;
        this.name = name;
    }

    @Override
    public void open(String path) {
        page.navigate(appUrl + path);
    }

    @Override
    public void type(String testId, String text) {
        page.getByTestId(testId).fill(text);
    }

    @Override
    public void click(String testId) {
        page.getByTestId(testId).click();
    }

    @Override
    public void clickAndWaitForPage(String testId) {
        // click() already waits for the navigation it starts to commit; then wait until the new page has loaded
        page.getByTestId(testId).click();
        page.waitForLoadState(LoadState.LOAD);
    }

    @Override
    public String text(String testId) {
        return element(testId).innerText();
    }

    @Override
    public List<String> texts(String testId) {
        return page.getByTestId(testId).allInnerTexts();
    }

    @Override
    public boolean isVisible(String testId) {
        return page.getByTestId(testId).first().isVisible();
    }

    @Override
    public String currentPath() {
        return URI.create(page.url()).getPath();
    }

    @Override
    public byte[] screenshot() {
        return page.screenshot();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }

    private Locator element(String testId) {
        Locator locator = page.getByTestId(testId);
        locator.waitFor();
        return locator;
    }
}
