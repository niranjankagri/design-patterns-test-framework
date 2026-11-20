package com.niranjankagri.patterns.clients.legacy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Stands in for a vendor's legacy SDK that the tests are not allowed to change: a connect/query style API that
 * returns raw {@code CODE|NAME|PRICE} records, names in upper case, prices in dollars, errors as {@code ERR|...}
 * strings instead of exceptions. {@link com.niranjankagri.patterns.clients.LegacyCatalogAdapter} hides all of that.
 */
public final class LegacyInventorySdk {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private URI endpoint;

    /** Must be called before {@link #query}; the SDK keeps the connection target as state. */
    public void connect(String baseUrl) {
        this.endpoint = URI.create(baseUrl + "/legacy/catalog");
    }

    /** Runs a query code ("ALL" lists everything) and returns the raw lines, or a single "ERR|..." line. */
    public String[] query(String code) {
        if (endpoint == null) {
            return new String[] {"ERR|NOT_CONNECTED"};
        }
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(endpoint + "?q=" + code))
                    .timeout(Duration.ofSeconds(30)).GET().build(), HttpResponse.BodyHandlers.ofString());
            return r.body().strip().split("\n");
        } catch (IOException e) {
            return new String[] {"ERR|IO|" + e.getMessage()};
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new String[] {"ERR|INTERRUPTED"};
        }
    }
}
