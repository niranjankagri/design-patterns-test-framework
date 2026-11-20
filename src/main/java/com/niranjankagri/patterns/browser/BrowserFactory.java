package com.niranjankagri.patterns.browser;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.niranjankagri.patterns.config.TestConfig;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

/**
 * Creates the browser a run asks for (Factory pattern). The run configuration names an engine, a browser and local
 * or remote; this class is the one place that turns that into a started browser, so tests and page objects never
 * contain driver setup and adding an engine or browser changes only this class.
 *
 * <pre>
 *   run config ─▶ BrowserFactory ─┬─ selenium, local  ─▶ ChromeDriver / FirefoxDriver / EdgeDriver ─┐
 *                                 ├─ selenium, remote ─▶ RemoteWebDriver (Selenium Grid)            ├─▶ Browser
 *                                 └─ playwright       ─▶ Chromium / Firefox / WebKit               ─┘
 * </pre>
 */
public final class BrowserFactory {

    /** Browsers each engine supports. */
    static final List<String> SELENIUM_BROWSERS = List.of("chrome", "firefox", "edge");
    static final List<String> PLAYWRIGHT_BROWSERS = List.of("chromium", "firefox", "webkit");

    /**
     * Chrome and Edge preferences that switch off the password manager. Without them, signing in with a password from
     * a known data breach (the demo password is one) opens a "change your password" dialog that swallows the next
     * clicks: the first add-to-cart click after signing in did nothing, while Playwright's Chromium (no password
     * manager) passed the same test.
     */
    static final Map<String, Object> NO_PASSWORD_MANAGER = Map.of(
            "credentials_enable_service", false,
            "profile.password_manager_enabled", false,
            "profile.password_manager_leak_detection", false);

    private BrowserFactory() {
    }

    /** Starts the browser this configuration describes. */
    public static Browser create(TestConfig config) {
        validate(config);
        return switch (config.engine()) {
            case SELENIUM -> new SeleniumBrowser(seleniumDriver(config), config.appUrl(), config.timeout(), config.label());
            case PLAYWRIGHT -> playwright(config);
        };
    }

    /**
     * Refuses combinations that cannot work before anything starts, with a message that says what to change
     * (for example {@code -Dengine=playwright -Dbrowser=chrome}).
     */
    static void validate(TestConfig config) {
        List<String> supported = config.engine() == TestConfig.Engine.SELENIUM ? SELENIUM_BROWSERS : PLAYWRIGHT_BROWSERS;
        if (!supported.contains(config.browser())) {
            throw new IllegalArgumentException("Browser '" + config.browser() + "' is not supported by "
                    + config.engine().name().toLowerCase() + "; use one of " + supported);
        }
        if (config.remote() && config.engine() == TestConfig.Engine.PLAYWRIGHT) {
            throw new IllegalArgumentException("remote=true is for Selenium Grid; Playwright runs its browsers locally");
        }
    }

    private static WebDriver seleniumDriver(TestConfig config) {
        Capabilities options = seleniumOptions(config);
        if (config.remote()) {
            try {
                return new RemoteWebDriver(config.gridUrl().toURL(), options);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("grid.url is not a valid URL: " + config.gridUrl(), e);
            }
        }
        // Selenium Manager finds or downloads the matching driver binary
        return switch (config.browser()) {
            case "firefox" -> new FirefoxDriver((FirefoxOptions) options);
            case "edge" -> new EdgeDriver((EdgeOptions) options);
            default -> new ChromeDriver((ChromeOptions) options);
        };
    }

    /** Browser options shared by local and remote runs. */
    static Capabilities seleniumOptions(TestConfig config) {
        return switch (config.browser()) {
            case "firefox" -> {
                FirefoxOptions o = new FirefoxOptions();
                if (config.headless()) {
                    o.addArguments("-headless");
                }
                yield o;
            }
            case "edge" -> {
                EdgeOptions o = new EdgeOptions();
                if (config.headless()) {
                    o.addArguments("--headless=new");
                }
                o.addArguments("--window-size=1280,900");
                o.setExperimentalOption("prefs", NO_PASSWORD_MANAGER);
                yield o;
            }
            default -> {
                ChromeOptions o = new ChromeOptions();
                if (config.headless()) {
                    o.addArguments("--headless=new");
                }
                // --no-sandbox and /dev/shm: needed in containers and on CI runners
                o.addArguments("--window-size=1280,900", "--no-sandbox", "--disable-dev-shm-usage");
                o.setExperimentalOption("prefs", NO_PASSWORD_MANAGER);
                yield o;
            }
        };
    }

    private static Browser playwright(TestConfig config) {
        Playwright playwright = Playwright.create();
        try {
            BrowserType type = switch (config.browser()) {
                case "firefox" -> playwright.firefox();
                case "webkit" -> playwright.webkit();
                default -> playwright.chromium();
            };
            com.microsoft.playwright.Browser browser = type.launch(
                    new BrowserType.LaunchOptions().setHeadless(config.headless()));
            return new PlaywrightBrowser(playwright, browser, config.appUrl(), config.timeout().toMillis(), config.label());
        } catch (RuntimeException e) {
            playwright.close();
            throw e;
        }
    }
}
