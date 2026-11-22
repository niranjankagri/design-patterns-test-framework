package com.niranjankagri.patterns.pages;

import com.niranjankagri.patterns.browser.Browser;

import java.util.List;

/** Checkout step 2: the items, item total, tax and total before the order is placed (Page Object pattern). */
public final class CheckoutOverviewPage {

    private final Browser browser;

    /** Waits until the overview is shown. */
    public CheckoutOverviewPage(Browser browser) {
        this.browser = browser;
        browser.text("total");
    }

    /** Names of the items to be ordered. */
    public List<String> itemNames() {
        return browser.texts("overview-item-name");
    }

    /** The item total as shown, for example "$29.99". */
    public String subtotal() {
        return browser.text("subtotal");
    }

    /** The tax as shown, for example "$2.40". */
    public String tax() {
        return browser.text("tax");
    }

    /** The total as shown, for example "$32.39". */
    public String total() {
        return browser.text("total");
    }

    /** Places the order. */
    public OrderCompletePage finish() {
        browser.clickAndWaitForPage("finish");
        return new OrderCompletePage(browser);
    }
}
