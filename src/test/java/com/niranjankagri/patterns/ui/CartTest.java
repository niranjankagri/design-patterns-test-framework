package com.niranjankagri.patterns.ui;

import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;
import com.niranjankagri.patterns.pages.CartPage;
import com.niranjankagri.patterns.pages.LoginPage;
import com.niranjankagri.patterns.pages.OrderCompletePage;
import com.niranjankagri.patterns.pages.ProductsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Cart and checkout. Expected totals are computed from the test data, not copied from the page. */
public class CartTest extends BaseUiTest {

    private ProductsPage products;

    @BeforeMethod // after BaseUiTest.startBrowser (TestNG runs base-class setup first); skipped if no browser started
    public void signIn() {
        User user = activeUser();
        products = LoginPage.open(browser).signIn(user.username(), user.password());
    }

    @Test(description = "TC-301 the cart lists the added products and their total")
    public void itemsAndTotal() {
        Product first = product(0);
        Product second = product(1);
        CartPage cart = products.add(first.id()).add(second.id()).openCart();

        assertThat(cart.itemNames()).containsExactly(first.name(), second.name());
        assertThat(cart.total()).isEqualTo(new Product("", "", first.priceCents() + second.priceCents()).displayPrice());
    }

    @Test(description = "TC-302 removing a product takes it out of the cart and the total")
    public void remove() {
        Product kept = product(2);
        Product removed = product(3);
        CartPage cart = products.add(kept.id()).add(removed.id()).openCart().remove(removed.id());

        assertThat(cart.itemNames()).containsExactly(kept.name());
        assertThat(cart.total()).isEqualTo(kept.displayPrice());
    }

    @Test(description = "TC-303 checkout confirms an order number and empties the cart")
    public void checkout() {
        OrderCompletePage done = products.add(product(0).id()).openCart().checkout();

        assertThat(done.title()).isEqualTo("Thank you for your order");
        assertThat(done.orderNumber()).matches("ORD-\\d{4}");
        assertThat(done.backToProducts().cartCount()).isZero();
    }
}
