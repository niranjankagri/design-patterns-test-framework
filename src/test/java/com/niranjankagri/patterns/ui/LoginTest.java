package com.niranjankagri.patterns.ui;

import com.niranjankagri.patterns.model.User;
import com.niranjankagri.patterns.pages.LoginPage;
import com.niranjankagri.patterns.pages.ProductsPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Sign-in and sign-out. The users come from the configured test-data source (json, csv or api). */
public class LoginTest extends BaseUiTest {

    @DataProvider
    public Object[][] users() {
        return DATA.users().stream().map(u -> new Object[] {u}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "users", description = "TC-101 each demo user gets the expected sign-in outcome")
    public void signInOutcome(User user) {
        // Given the sign-in page
        LoginPage login = LoginPage.open(browser);

        if (user.canSignIn()) {
            // When an active user signs in, then the product list greets them
            ProductsPage products = login.signIn(user.username(), user.password());
            assertThat(products.path()).isEqualTo("/products");
            assertThat(products.welcome()).isEqualTo("Signed in as " + user.username());
        } else {
            // When a locked user signs in, then the locked message is shown and they stay on the sign-in page
            LoginPage refused = login.signInExpectingError(user.username(), user.password());
            assertThat(refused.error()).isEqualTo("This user has been locked out.");
        }
    }

    @Test(description = "TC-102 a wrong password is refused without saying which field was wrong")
    public void wrongPassword() {
        LoginPage refused = LoginPage.open(browser).signInExpectingError(activeUser().username(), "not-the-password");
        assertThat(refused.error()).isEqualTo("User name and password do not match any user.");
    }

    @Test(description = "TC-103 empty fields are reported as required")
    public void emptyFields() {
        LoginPage refused = LoginPage.open(browser).signInExpectingError("", "");
        assertThat(refused.error()).isEqualTo("User name and password are required.");
    }

    @Test(description = "TC-104 signing out returns to the sign-in page and ends the session")
    public void signOut() {
        User user = activeUser();
        LoginPage afterSignOut = LoginPage.open(browser).signIn(user.username(), user.password()).signOut();
        assertThat(afterSignOut.hasError()).isFalse();

        // The product list is no longer reachable without signing in again
        browser.open("/products");
        assertThat(browser.currentPath()).isEqualTo("/login");
    }
}
