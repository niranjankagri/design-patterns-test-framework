package com.niranjankagri.patterns.pages;

import com.niranjankagri.patterns.browser.Browser;

/** The confirmation after checkout (Page Object pattern). */
public final class OrderCompletePage {

    private final Browser browser;

    /** Waits until the confirmation is shown. */
    public OrderCompletePage(Browser browser) {
        this.browser = browser;
        browser.text("complete-title");
    }

    /** The heading, for example "Thank you for your order". */
    public String title() {
        return browser.text("complete-title");
    }

    /** The order number, for example "ORD-4821". */
    public String orderNumber() {
        return browser.text("order-number");
    }

    /** Back to the product list. */
    public ProductsPage backToProducts() {
        browser.clickAndWaitForPage("back-to-products");
        return new ProductsPage(browser);
    }
}
