# The five patterns, in detail

Each section says what the pattern is, where it is in this code, why it is used here, what it costs, and how to
extend it. Class names link to the source.

## 01. Page Object

**What:** one class per page that knows the page's elements and the actions a user takes there. Tests call
`login.signIn(user, password)` instead of finding fields and buttons themselves.

**Where:** [`LoginPage`](../src/main/java/com/niranjankagri/patterns/pages/LoginPage.java),
[`ProductsPage`](../src/main/java/com/niranjankagri/patterns/pages/ProductsPage.java),
[`CartPage`](../src/main/java/com/niranjankagri/patterns/pages/CartPage.java),
[`OrderCompletePage`](../src/main/java/com/niranjankagri/patterns/pages/OrderCompletePage.java).

**Rules followed here:**

- **No assertions in page objects.** A page returns what it shows (`error()`, `total()`, `names()`); the test decides
  what is right. The same page object then serves a positive and a negative test.
- **Actions return the next page.** `signIn` returns a `ProductsPage`; `signInExpectingError` returns a `LoginPage`.
  The test reads as a path through the application, and the compiler stops a test from calling a cart method on the
  login page.
- **A page checks it is shown** in its constructor (it waits for its title), so a wrong navigation fails at the step
  that caused it, not three steps later.
- **Page objects talk to the `Browser` interface**, not to Selenium or Playwright. That is why the same page objects
  run on both engines.

**Cost:** one more class per page. Worth it from the second test that uses a page.

## 02. Factory

**What:** one place creates objects whose concrete type depends on configuration.

**Where:** [`BrowserFactory.create(config)`](../src/main/java/com/niranjankagri/patterns/browser/BrowserFactory.java):

```text
run config ─▶ BrowserFactory ─┬─ selenium, local  ─▶ ChromeDriver / FirefoxDriver / EdgeDriver ─┐
                              ├─ selenium, remote ─▶ RemoteWebDriver (Selenium Grid)            ├─▶ Browser
                              └─ playwright       ─▶ Chromium / Firefox / WebKit               ─┘
```

**Why:** without it, every base test or hook grows its own `if (browser.equals("firefox"))`. Here the whole decision,
the browser options (headless, window size, sandbox flags for CI, the password manager switched off) and the
validation of combinations are in one class with its own tests (TC-601..603).

**Validation before start:** `-Dengine=playwright -Dbrowser=chrome` fails immediately with
"use one of [chromium, firefox, webkit]", instead of failing later with a driver error.

**Extend:** a new browser or a cloud grid is one more branch in the factory; no test changes.

## 03. Strategy

**What:** an interface with interchangeable implementations of one algorithm, picked at run time.

**Where:** [`TestDataSource`](../src/main/java/com/niranjankagri/patterns/data/TestDataSource.java) with
[`JsonTestDataSource`](../src/main/java/com/niranjankagri/patterns/data/JsonTestDataSource.java),
[`CsvTestDataSource`](../src/main/java/com/niranjankagri/patterns/data/CsvTestDataSource.java) and
[`ApiTestDataSource`](../src/main/java/com/niranjankagri/patterns/data/ApiTestDataSource.java), selected by
[`TestDataSources`](../src/main/java/com/niranjankagri/patterns/data/TestDataSources.java) from `data.source`.

**Why:** test data often starts in a JSON file, moves to a spreadsheet a business analyst maintains (CSV), and later
comes from an environment's test-data service (API). With a strategy, that move changes one setting. The tests ask
for `DATA.users()` and `DATA.products()` and never know the source.

**Checked by:** TC-501 (JSON and CSV return the same data), TC-503 (the API returns the same as the files), and CI,
which runs the UI tests with each source on different engines.

**Strategy vs Factory:** `TestDataSources.of(name)` is a small factory that *picks* the strategy; the strategy is the
interchangeable *behaviour*. Most real code combines the two like this.

## 04. Adapter

**What:** a class that translates one API into the interface the client code expects.

**Where, twice:**

1. **Browser engines.** [`Browser`](../src/main/java/com/niranjankagri/patterns/browser/Browser.java) is the interface
   page objects use. [`SeleniumBrowser`](../src/main/java/com/niranjankagri/patterns/browser/SeleniumBrowser.java)
   translates it to WebDriver (adding the explicit waits Selenium needs);
   [`PlaywrightBrowser`](../src/main/java/com/niranjankagri/patterns/browser/PlaywrightBrowser.java) translates it to
   Playwright (which waits by itself). The interface is deliberately small: only what the page objects need.
2. **Catalog clients.** The shop offers the same catalog through three different APIs. The tests use
   [`CatalogClient`](../src/main/java/com/niranjankagri/patterns/clients/CatalogClient.java); three adapters translate:

| Adapter | Client API | Translation |
|---|---|---|
| [`RestCatalogAdapter`](../src/main/java/com/niranjankagri/patterns/clients/RestCatalogAdapter.java) | `GET /api/products`, JSON | format only (fields already match) |
| [`SoapCatalogAdapter`](../src/main/java/com/niranjankagri/patterns/clients/SoapCatalogAdapter.java) | `POST /soap/catalog`, XML envelope | code → id, title → name, dollars → cents; XML parsed without DTDs (no XXE) |
| [`LegacyCatalogAdapter`](../src/main/java/com/niranjankagri/patterns/clients/LegacyCatalogAdapter.java) | [`LegacyInventorySdk`](../src/main/java/com/niranjankagri/patterns/clients/legacy/LegacyInventorySdk.java): `connect()`, `query("ALL")`, `CODE\|NAME\|PRICE` lines, `ERR\|...` strings | connects, turns error strings into exceptions, splits lines, title-cases names, dollars → cents |

One set of checks (TC-401..404) runs against all three. A fourth API is one more adapter and no new test.

**Adapter vs Facade:** an adapter makes an existing API fit an interface that already exists; a facade invents a
simpler interface over a complex subsystem. `Browser` is both a little: it is the target of two adapters and simpler
than either engine's API.

## 05. Observer

**What:** a subject sends events to any number of subscribed observers, which do not know about each other.

**Where:** [`TestEventBus`](../src/main/java/com/niranjankagri/patterns/events/TestEventBus.java) (subject),
[`TestEventListener`](../src/main/java/com/niranjankagri/patterns/events/TestEventListener.java) (observer),
[`TestEvent`](../src/main/java/com/niranjankagri/patterns/events/TestEvent.java) (message). Listeners:
[`ReportListener`](../src/main/java/com/niranjankagri/patterns/events/ReportListener.java) (Markdown report),
[`LogListener`](../src/main/java/com/niranjankagri/patterns/events/LogListener.java),
[`MetricsListener`](../src/main/java/com/niranjankagri/patterns/events/MetricsListener.java) (JSON) and
[`ScreenshotListener`](../src/main/java/com/niranjankagri/patterns/events/ScreenshotListener.java).
[`TestNgEventBridge`](../src/main/java/com/niranjankagri/patterns/events/TestNgEventBridge.java) turns TestNG's
callbacks into events.

```text
TestNG ─▶ TestNgEventBridge ─▶ TestEventBus ─┬─▶ ReportListener      report.md
BaseUiTest (failure) ─ screenshot event ─────┤─▶ LogListener         one line per test
                                             ├─▶ MetricsListener     metrics.json
                                             └─▶ ScreenshotListener  screenshots/*.png
```

**Design choices:**

- **Listeners are found by Java's ServiceLoader** (`META-INF/services/...TestEventListener`): adding a listener is a
  new class and one line, with no change to the bus or the tests.
- **A failing listener is isolated** (TC-702): a broken report must not hide the other outputs or fail the test.
- **Only the bridge knows TestNG.** The listeners know test events only, so moving to JUnit means one new bridge.
- **The screenshot is an event too.** `BaseUiTest` publishes it after a failure; it does not know where it is saved.

## What was left out on purpose

- **Singleton** for the driver: a static driver breaks parallel runs. Each test gets its own browser from the factory.
- **Builder** for test data: the records here are small; a builder pays off when objects have many optional fields.
- **Screenplay**: a step beyond page objects (actors, tasks, questions); more structure than five pages need.
