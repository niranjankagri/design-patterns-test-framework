package com.niranjankagri.patterns.ui;

import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;
import com.niranjankagri.patterns.pages.LoginPage;
import com.niranjankagri.patterns.pages.ProductsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The product list against the expected catalog of the test data. */
public class ProductsTest extends BaseUiTest {

    private ProductsPage products;

    @BeforeMethod // after BaseUiTest.startBrowser (TestNG runs base-class setup first); skipped if no browser started
    public void signIn() {
        User user = activeUser();
        products = LoginPage.open(browser).signIn(user.username(), user.password());
    }

    @Test(description = "TC-201 the product list shows the expected catalog with names and prices")
    public void catalog() {
        assertThat(products.names()).containsExactlyElementsOf(DATA.products().stream().map(Product::name).toList());
        assertThat(products.prices()).containsExactlyElementsOf(DATA.products().stream().map(Product::displayPrice).toList());
    }

    @Test(description = "TC-202 adding products counts them in the cart link")
    public void addToCart() {
        assertThat(products.cartCount()).isZero();
        ProductsPage after = products.add(product(0).id()).add(product(1).id());
        assertThat(after.cartCount()).isEqualTo(2);
    }
}
