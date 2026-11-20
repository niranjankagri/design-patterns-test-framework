package com.niranjankagri.patterns.ui;

import com.niranjankagri.patterns.browser.Browser;
import com.niranjankagri.patterns.browser.BrowserFactory;
import com.niranjankagri.patterns.config.TestConfig;
import com.niranjankagri.patterns.data.TestDataSource;
import com.niranjankagri.patterns.data.TestDataSources;
import com.niranjankagri.patterns.events.TestEvent;
import com.niranjankagri.patterns.events.TestEventBus;
import com.niranjankagri.patterns.events.TestNgEventBridge;
import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Shared setup of the UI tests: the run configuration, the test data (Strategy) and a fresh browser per test from the
 * Factory. After a failure it publishes a screenshot event (Observer); which listener saves it is not its business.
 */
public abstract class BaseUiTest {

    protected static final TestConfig CONFIG = TestConfig.load();
    protected static final TestDataSource DATA = TestDataSources.from(CONFIG);

    protected Browser browser;

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        browser = BrowserFactory.create(CONFIG);
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser(ITestResult result) {
        if (browser == null) {
            return;
        }
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                TestEventBus.global().publish(TestEvent.screenshot(TestNgEventBridge.name(result), browser.name(),
                        browser.screenshot()));
            }
        } finally {
            browser.close();
            browser = null;
        }
    }

    /** The first user of the test data who can sign in. */
    protected static User activeUser() {
        return DATA.users().stream().filter(User::canSignIn).findFirst()
                .orElseThrow(() -> new IllegalStateException(DATA.name() + " test data has no active user"));
    }

    /** A product of the test data by position, so tests do not hard-code ids. */
    protected static Product product(int index) {
        return DATA.products().get(index);
    }
}
