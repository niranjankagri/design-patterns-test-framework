package com.niranjankagri.patterns.clients;

import com.niranjankagri.patterns.clients.legacy.LegacyInventorySdk;
import com.niranjankagri.patterns.model.Product;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapts the legacy inventory SDK to {@link CatalogClient}: connects it, turns its "ERR|..." strings into exceptions,
 * splits its {@code CODE|NAME|PRICE} lines, converts dollars to cents and upper-case names to the shop's display
 * names (title case). The SDK itself is used as it is.
 */
public final class LegacyCatalogAdapter implements CatalogClient {

    private final LegacyInventorySdk sdk;

    /** @param appUrl the shop's base URL */
    public LegacyCatalogAdapter(URI appUrl) {
        this(new LegacyInventorySdk(), appUrl);
    }

    LegacyCatalogAdapter(LegacyInventorySdk sdk, URI appUrl) {
        this.sdk = sdk;
        this.sdk.connect(appUrl.toString());
    }

    @Override
    public String name() {
        return "legacy";
    }

    @Override
    public List<Product> products() {
        String[] lines = sdk.query("ALL");
        if (lines.length > 0 && lines[0].startsWith("ERR|")) {
            throw new IllegalStateException("Legacy inventory SDK error: " + lines[0]);
        }
        List<Product> products = new ArrayList<>();
        for (String line : lines) {
            String[] f = line.split("\\|");
            if (f.length != 3) {
                throw new IllegalStateException("Legacy record has " + f.length + " fields, expected 3: " + line);
            }
            products.add(new Product(f[0], titleCase(f[1]), SoapCatalogAdapter.cents(f[2])));
        }
        return List.copyOf(products);
    }

    /** "BOLT T-SHIRT" → "Bolt T-Shirt": capital letter after a space or hyphen, the rest lower case. */
    static String titleCase(String upper) {
        StringBuilder out = new StringBuilder(upper.length());
        boolean capital = true;
        for (char c : upper.toLowerCase(Locale.ROOT).toCharArray()) {
            out.append(capital ? Character.toUpperCase(c) : c);
            capital = c == ' ' || c == '-';
        }
        return out.toString();
    }
}
