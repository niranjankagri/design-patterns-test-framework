package com.niranjankagri.patterns.pages;

import com.niranjankagri.patterns.browser.Browser;

/** Checkout step 1: first name, last name and postal code (Page Object pattern). */
public final class CheckoutInfoPage {

    private final Browser browser;

    /** Waits until the form is shown. */
    public CheckoutInfoPage(Browser browser) {
        this.browser = browser;
        browser.text("checkout-title");
    }

    /** Fills the three fields; each is required. */
    public CheckoutInfoPage enter(String firstName, String lastName, String postalCode) {
        browser.type("first-name", firstName);
        browser.type("last-name", lastName);
        browser.type("postal-code", postalCode);
        return this;
    }

    /** Submits the form; with every field filled the overview follows. */
    public CheckoutOverviewPage continueToOverview() {
        browser.clickAndWaitForPage("continue");
        return new CheckoutOverviewPage(browser);
    }
}
