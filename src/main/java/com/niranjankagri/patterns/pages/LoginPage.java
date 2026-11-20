package com.niranjankagri.patterns.pages;

import com.niranjankagri.patterns.browser.Browser;

/**
 * The sign-in page (Page Object pattern). It knows the page's elements and the actions a user takes there; it holds
 * no assertions, so tests decide what is right. Each action returns the page the user lands on.
 */
public final class LoginPage {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SIGN_IN = "login-button";
    private static final String ERROR = "login-error";

    private final Browser browser;

    /** For a browser that already shows the sign-in page. */
    public LoginPage(Browser browser) {
        this.browser = browser;
        browser.text("login-title");
    }

    /** Opens the sign-in page. */
    public static LoginPage open(Browser browser) {
        browser.open("/login");
        return new LoginPage(browser);
    }

    /** Signs in a user who can sign in, and returns the product list. */
    public ProductsPage signIn(String username, String password) {
        submit(username, password);
        return new ProductsPage(browser);
    }

    /** Tries to sign in with data that should be refused, and stays on this page. */
    public LoginPage signInExpectingError(String username, String password) {
        submit(username, password);
        return new LoginPage(browser);
    }

    /** The error message shown under the title. */
    public String error() {
        return browser.text(ERROR);
    }

    /** Whether an error message is shown. */
    public boolean hasError() {
        return browser.isVisible(ERROR);
    }

    private void submit(String username, String password) {
        browser.type(USERNAME, username);
        browser.type(PASSWORD, password);
        browser.clickAndWaitForPage(SIGN_IN);
    }
}
