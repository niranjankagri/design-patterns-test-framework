package com.niranjankagri.patterns.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niranjankagri.patterns.model.Product;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/** Adapts the shop's REST API ({@code GET /api/products}, JSON, prices in cents) to {@link CatalogClient}. */
public final class RestCatalogAdapter implements CatalogClient {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final URI appUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    /** @param appUrl the shop's base URL */
    public RestCatalogAdapter(URI appUrl) {
        this.appUrl = appUrl;
    }

    @Override
    public String name() {
        return "rest";
    }

    @Override
    public List<Product> products() {
        String body = Http.send(http, HttpRequest.newBuilder(URI.create(appUrl + "/api/products")).GET());
        try {
            // The JSON fields already match the Product record: nothing to translate but the format
            return List.copyOf(JSON.readValue(body, new TypeReference<List<Product>>() { }));
        } catch (IOException e) {
            throw new IllegalStateException("The REST catalog answered something that is not a product list", e);
        }
    }

    /** Small shared helper: send a request, insist on HTTP 200, return the body. */
    static final class Http {

        private Http() {
        }

        static String send(HttpClient http, HttpRequest.Builder request) {
            try {
                HttpResponse<String> r = http.send(request.timeout(Duration.ofSeconds(30)).build(),
                        HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() != 200) {
                    throw new IllegalStateException(r.request().uri() + " answered HTTP " + r.statusCode() + ": " + r.body());
                }
                return r.body();
            } catch (IOException e) {
                throw new IllegalStateException("Request failed: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted", e);
            }
        }
    }
}
