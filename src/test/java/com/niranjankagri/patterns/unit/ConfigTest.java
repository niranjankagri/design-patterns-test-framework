package com.niranjankagri.patterns.unit;

import com.niranjankagri.patterns.config.TestConfig;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The run configuration: file defaults, overridden by system properties. */
public class ConfigTest {

    private static final String[] KEYS = {"engine", "browser", "app.url", "data.source"};
    private final String[] saved = new String[KEYS.length];

    @Test(description = "TC-604 system properties override framework.properties, and the browser default follows the engine")
    public void overrides() {
        save();
        System.setProperty("engine", "playwright");
        System.clearProperty("browser");
        System.setProperty("app.url", "https://shop.example/");
        System.setProperty("data.source", "CSV");

        TestConfig config = TestConfig.load();

        assertThat(config.engine()).isEqualTo(TestConfig.Engine.PLAYWRIGHT);
        // No browser given anywhere: the default follows the engine
        assertThat(config.browser()).isEqualTo("chromium");
        assertThat(config.appUrl()).hasToString("https://shop.example");
        assertThat(config.dataSource()).isEqualTo("csv");
        assertThat(config.label()).isEqualTo("playwright/chromium");
    }

    private void save() {
        for (int i = 0; i < KEYS.length; i++) {
            saved[i] = System.getProperty(KEYS[i]);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void restore() {
        for (int i = 0; i < KEYS.length; i++) {
            if (saved[i] == null) {
                System.clearProperty(KEYS[i]);
            } else {
                System.setProperty(KEYS[i], saved[i]);
            }
        }
    }
}
