package com.niranjankagri.patterns.clients;

import com.niranjankagri.patterns.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * The one catalog contract the tests use (Adapter pattern: the target interface). The shop offers its catalog
 * through three very different client APIs (REST/JSON, SOAP-style XML and a legacy text SDK); one adapter per API
 * translates it into this interface, so the same tests run against all three.
 */
public interface CatalogClient {

    /** A short name for reports: rest, soap or legacy. */
    String name();

    /** Every product, in the shop's order. */
    List<Product> products();

    /** The product with this code, if there is one. */
    default Optional<Product> product(String id) {
        return products().stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
