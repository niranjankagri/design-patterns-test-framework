package com.niranjankagri.patterns.browser;

import com.niranjankagri.patterns.config.TestConfig;
import com.niranjankagri.patterns.config.TestConfig.Engine;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The factory's decisions, checked without starting a browser (same package: validate and options are internal). */
public class BrowserFactoryTest {

    @Test(description = "TC-601 a browser the engine does not support is refused with the supported list")
    public void unsupportedBrowser() {
        assertThatThrownBy(() -> BrowserFactory.validate(config(Engine.PLAYWRIGHT, "chrome", false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Browser 'chrome' is not supported by playwright; use one of [chromium, firefox, webkit]");
        assertThatThrownBy(() -> BrowserFactory.validate(config(Engine.SELENIUM, "webkit", false)))
                .hasMessageContaining("use one of [chrome, firefox, edge]");
    }

    @Test(description = "TC-602 remote runs are for Selenium Grid only")
    public void remoteOnlyForSelenium() {
        assertThatThrownBy(() -> BrowserFactory.validate(config(Engine.PLAYWRIGHT, "chromium", true)))
                .hasMessageContaining("Selenium Grid");
        BrowserFactory.validate(config(Engine.SELENIUM, "firefox", true));
    }

    @Test(description = "TC-603 local and remote Selenium runs get the same browser options, password manager off")
    public void seleniumOptions() {
        assertThat(BrowserFactory.seleniumOptions(config(Engine.SELENIUM, "chrome", true)))
                .isInstanceOf(ChromeOptions.class)
                .satisfies(o -> assertThat(o.asMap().toString()).contains("--headless=new", "--window-size=1280,900",
                        "profile.password_manager_leak_detection=false"));
        assertThat(BrowserFactory.seleniumOptions(config(Engine.SELENIUM, "edge", true)))
                .isInstanceOf(EdgeOptions.class)
                .satisfies(o -> assertThat(o.asMap().toString()).contains("--headless=new", "--no-sandbox",
                        "--disable-dev-shm-usage", "--no-first-run", "profile.password_manager_leak_detection=false"));
        assertThat(BrowserFactory.seleniumOptions(config(Engine.SELENIUM, "firefox", false)))
                .isInstanceOf(FirefoxOptions.class)
                .satisfies(o -> assertThat(o.asMap().toString()).contains("-headless"));
    }

    private static TestConfig config(Engine engine, String browser, boolean remote) {
        return new TestConfig(engine, browser, true, remote, URI.create("http://localhost:4444"),
                URI.create("http://localhost:8080"), "json", Duration.ofSeconds(5));
    }
}
