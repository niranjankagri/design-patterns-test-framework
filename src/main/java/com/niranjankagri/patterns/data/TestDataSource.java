package com.niranjankagri.patterns.data;

import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;

import java.util.List;

/**
 * Where test data comes from (Strategy pattern: the strategy interface). Tests ask for users and products and do
 * not know whether they were read from JSON, CSV or the shop's test-data API; the run configuration
 * ({@code -Ddata.source=csv}) picks the implementation through {@link TestDataSources}.
 */
public interface TestDataSource {

    /** A short name for reports: json, csv or api. */
    String name();

    /** The demo users with their expected sign-in outcome. */
    List<User> users();

    /** The catalog the shop is expected to show, in display order. */
    List<Product> products();
}
