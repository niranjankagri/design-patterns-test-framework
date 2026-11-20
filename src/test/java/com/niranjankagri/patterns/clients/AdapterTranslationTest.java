package com.niranjankagri.patterns.clients;

import org.testng.annotations.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The translations inside the adapters, without a shop (same package: the helpers are internal). */
public class AdapterTranslationTest {

    @Test(description = "TC-801 legacy upper-case names become the shop's display names")
    public void titleCase() {
        assertThat(LegacyCatalogAdapter.titleCase("BOLT T-SHIRT")).isEqualTo("Bolt T-Shirt");
        assertThat(LegacyCatalogAdapter.titleCase("FLEECE JACKET")).isEqualTo("Fleece Jacket");
        assertThat(LegacyCatalogAdapter.titleCase("BACKPACK")).isEqualTo("Backpack");
    }

    @Test(description = "TC-802 prices in dollars become exact cents")
    public void cents() {
        assertThat(SoapCatalogAdapter.cents("9.99")).isEqualTo(999);
        assertThat(SoapCatalogAdapter.cents("49.99")).isEqualTo(4999);
        assertThat(SoapCatalogAdapter.cents("0.10")).isEqualTo(10);
        // 0.1 + 0.2 style rounding cannot happen: a third decimal is refused, not rounded
        assertThatThrownBy(() -> SoapCatalogAdapter.cents("1.005")).isInstanceOf(ArithmeticException.class);
    }

    @Test(description = "TC-803 a legacy SDK error string becomes an exception with the SDK's code")
    public void legacyError() {
        // Port 1: nothing listens, so the SDK answers "ERR|IO|..." instead of throwing
        LegacyCatalogAdapter adapter = new LegacyCatalogAdapter(URI.create("http://localhost:1"));
        assertThatThrownBy(adapter::products).isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Legacy inventory SDK error: ERR|IO");
    }
}
