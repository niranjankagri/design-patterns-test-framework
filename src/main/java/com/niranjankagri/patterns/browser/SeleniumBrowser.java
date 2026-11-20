package com.niranjankagri.patterns.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/**
 * Adapter from the {@link Browser} interface to Selenium WebDriver. Selenium does not wait for elements by itself,
 * so every lookup goes through an explicit wait here, and the page objects never see a WebDriverWait.
 */
public final class SeleniumBrowser implements Browser {

    /** How long a page may take to load. */
    static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);

    private final WebDriver driver;
    private final URI appUrl;
    private final WebDriverWait wait;
    private final Duration timeout;
    private final String name;

    SeleniumBrowser(WebDriver driver, URI appUrl, Duration timeout, String name) {
        this.driver = driver;
        this.appUrl = appUrl;
        this.wait = new WebDriverWait(driver, timeout);
        // A page that does not load fails after 30 s with a clear message, not after the client's 3-minute read timeout
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        this.timeout = timeout;
        this.name = name;
    }

    @Override
    public void open(String path) {
        driver.get(appUrl + path);
    }

    @Override
    public void type(String testId, String text) {
        WebElement field = visible(testId);
        field.clear();
        field.sendKeys(text);
    }

    @Override
    public void click(String testId) {
        wait.until(ExpectedConditions.elementToBeClickable(by(testId))).click();
    }

    @Override
    public void clickAndWaitForPage(String testId) {
        // Mark the current document, click, then wait for a fully loaded document without the mark. This also works
        // when the next page has the same URL (adding to the cart reloads /products), and it never touches an element
        // of the old page while the browser is replacing it.
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.__beforeClick = true;");
        click(testId);
        // Its own wait: while the page is swapped, a script call can fail, which here only means "not yet"
        new WebDriverWait(driver, timeout).ignoring(WebDriverException.class).until(d -> Boolean.TRUE.equals(js.executeScript(
                "return window.__beforeClick === undefined && document.readyState === 'complete';")));
    }

    @Override
    public String text(String testId) {
        return visible(testId).getText();
    }

    @Override
    public List<String> texts(String testId) {
        return driver.findElements(by(testId)).stream().map(WebElement::getText).toList();
    }

    @Override
    public boolean isVisible(String testId) {
        return driver.findElements(by(testId)).stream().anyMatch(WebElement::isDisplayed);
    }

    @Override
    public String currentPath() {
        return URI.create(driver.getCurrentUrl()).getPath();
    }

    @Override
    public byte[] screenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void close() {
        driver.quit();
    }

    private WebElement visible(String testId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by(testId)));
    }

    private static By by(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
