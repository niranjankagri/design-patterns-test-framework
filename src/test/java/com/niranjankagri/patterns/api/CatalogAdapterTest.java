package com.niranjankagri.patterns.api;

import com.niranjankagri.patterns.clients.CatalogClient;
import com.niranjankagri.patterns.clients.CatalogClients;
import com.niranjankagri.patterns.config.TestConfig;
import com.niranjankagri.patterns.data.JsonTestDataSource;
import com.niranjankagri.patterns.model.Product;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One set of catalog checks, run against all three client APIs through their adapters (REST, SOAP-style XML, legacy
 * SDK). The checks only know {@link CatalogClient}; a fourth API would be one more adapter and no new test.
 */
public class CatalogAdapterTest {

    private static final TestConfig CONFIG = TestConfig.load();
    private static final List<Product> EXPECTED = new JsonTestDataSource().products();

    @DataProvider
    public Object[][] clients() {
        return CatalogClients.all(CONFIG.appUrl()).stream().map(c -> new Object[] {c}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "clients", description = "TC-401 each client API lists the expected catalog")
    public void listsCatalog(CatalogClient client) {
        assertThat(client.products()).containsExactlyElementsOf(EXPECTED);
    }

    @Test(dataProvider = "clients", description = "TC-402 each client API finds a product by its code")
    public void findsProduct(CatalogClient client) {
        Product expected = EXPECTED.get(2);
        assertThat(client.product(expected.id())).contains(expected);
    }

    @Test(dataProvider = "clients", description = "TC-403 each client API reports an unknown code as not found")
    public void unknownProduct(CatalogClient client) {
        assertThat(client.product("P-999")).isEmpty();
    }

    @Test(description = "TC-404 the three client APIs return exactly the same products")
    public void adaptersAgree() {
        List<List<Product>> results = CatalogClients.all(CONFIG.appUrl()).stream().map(CatalogClient::products).toList();
        assertThat(results).allSatisfy(r -> assertThat(r).isEqualTo(results.get(0)));
    }
}
