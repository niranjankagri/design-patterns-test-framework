package com.niranjankagri.patterns.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Reads test data from the shop itself: users from its test-data service ({@code /api/test-data/users}) and the
 * catalog from its REST API. Useful when the data changes with the environment; files are fixed per commit.
 */
public final class ApiTestDataSource implements TestDataSource {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final URI appUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    /** @param appUrl the shop's base URL */
    public ApiTestDataSource(URI appUrl) {
        this.appUrl = appUrl;
    }

    @Override
    public String name() {
        return "api";
    }

    @Override
    public List<User> users() {
        return get("/api/test-data/users", new TypeReference<>() { });
    }

    @Override
    public List<Product> products() {
        return get("/api/products", new TypeReference<>() { });
    }

    private <T> List<T> get(String path, TypeReference<List<T>> type) {
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(appUrl + path))
                    .timeout(Duration.ofSeconds(30)).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) {
                throw new IllegalStateException(path + " answered HTTP " + r.statusCode());
            }
            return List.copyOf(JSON.readValue(r.body(), type));
        } catch (IOException e) {
            throw new IllegalStateException("Test data could not be read from " + appUrl + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading test data", e);
        }
    }
}
