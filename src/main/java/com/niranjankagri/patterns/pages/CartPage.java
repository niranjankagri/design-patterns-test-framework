package com.niranjankagri.patterns.pages;

import com.niranjankagri.patterns.browser.Browser;

import java.util.List;

/** The cart: items, total, remove and checkout (Page Object pattern). */
public final class CartPage {

    private final Browser browser;

    /** Waits until the cart is shown. */
    public CartPage(Browser browser) {
        this.browser = browser;
        browser.text("cart-title");
    }

    /** Names of the items in the cart, in the order they were added. */
    public List<String> itemNames() {
        return browser.texts("cart-item-name");
    }

    /** The total as shown, for example "$39.98". */
    public String total() {
        return browser.text("cart-total");
    }

    /** Removes one of this product; the cart reloads. */
    public CartPage remove(String productId) {
        browser.clickAndWaitForPage("remove-" + productId);
        return new CartPage(browser);
    }

    /** Starts the checkout: the form for first name, last name and postal code. */
    public CheckoutInfoPage checkout() {
        browser.clickAndWaitForPage("checkout");
        return new CheckoutInfoPage(browser);
    }

    /** Back to the product list. */
    public ProductsPage continueShopping() {
        browser.clickAndWaitForPage("continue-shopping");
        return new ProductsPage(browser);
    }
}
