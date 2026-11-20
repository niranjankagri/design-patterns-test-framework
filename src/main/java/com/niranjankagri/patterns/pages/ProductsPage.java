package com.niranjankagri.patterns.pages;

import com.niranjankagri.patterns.browser.Browser;

import java.util.List;

/** The product list with the cart counter and sign-out (Page Object pattern). */
public final class ProductsPage {

    private final Browser browser;

    /** Waits until the product list is shown. */
    public ProductsPage(Browser browser) {
        this.browser = browser;
        browser.text("products-title");
    }

    /** The "Signed in as ..." line. */
    public String welcome() {
        return browser.text("welcome");
    }

    /** Product names in page order. */
    public List<String> names() {
        return browser.texts("product-name");
    }

    /** Prices as shown, for example "$9.99", in page order. */
    public List<String> prices() {
        return browser.texts("product-price");
    }

    /** Adds a product to the cart; the page reloads with the new count. */
    public ProductsPage add(String productId) {
        browser.clickAndWaitForPage("add-" + productId);
        return new ProductsPage(browser);
    }

    /** The number in the cart link. */
    public int cartCount() {
        return Integer.parseInt(browser.text("cart-count"));
    }

    /** Opens the cart. */
    public CartPage openCart() {
        browser.clickAndWaitForPage("cart-link");
        return new CartPage(browser);
    }

    /** Signs out and returns the sign-in page. */
    public LoginPage signOut() {
        browser.clickAndWaitForPage("logout");
        return new LoginPage(browser);
    }

    /** The path of the page, for checks that the user landed here. */
    public String path() {
        return browser.currentPath();
    }
}
