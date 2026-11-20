package com.niranjankagri.patterns.clients;

import java.net.URI;
import java.util.List;

/** All catalog adapters for one shop, so a test can run the same checks against each client API. */
public final class CatalogClients {

    private CatalogClients() {
    }

    /** REST, SOAP-style and legacy adapters for the shop at this URL. */
    public static List<CatalogClient> all(URI appUrl) {
        return List.of(new RestCatalogAdapter(appUrl), new SoapCatalogAdapter(appUrl), new LegacyCatalogAdapter(appUrl));
    }
}
