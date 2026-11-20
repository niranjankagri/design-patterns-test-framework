package com.niranjankagri.patterns.data;

import com.niranjankagri.patterns.config.TestConfig;

import java.net.URI;

/** Picks the test-data strategy a run asks for ({@code data.source} = json, csv or api). */
public final class TestDataSources {

    private TestDataSources() {
    }

    /** The data source of this configuration. */
    public static TestDataSource from(TestConfig config) {
        return of(config.dataSource(), config.appUrl());
    }

    /** The data source with this name; {@code appUrl} is used by the api source only. */
    public static TestDataSource of(String name, URI appUrl) {
        return switch (name) {
            case "json" -> new JsonTestDataSource();
            case "csv" -> new CsvTestDataSource();
            case "api" -> new ApiTestDataSource(appUrl);
            default -> throw new IllegalArgumentException("Unknown data.source '" + name + "'; use json, csv or api");
        };
    }
}
