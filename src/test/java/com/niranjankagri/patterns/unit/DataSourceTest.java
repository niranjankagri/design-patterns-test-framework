package com.niranjankagri.patterns.unit;

import com.niranjankagri.patterns.data.CsvTestDataSource;
import com.niranjankagri.patterns.data.JsonTestDataSource;
import com.niranjankagri.patterns.data.TestDataSource;
import com.niranjankagri.patterns.data.TestDataSources;
import com.niranjankagri.patterns.model.User;
import org.testng.annotations.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The file-based test-data strategies (the api strategy needs the shop: see ApiDataSourceTest). */
public class DataSourceTest {

    private static final URI UNUSED = URI.create("http://localhost:1");

    @Test(description = "TC-501 the json and csv data sources return the same users and products")
    public void jsonAndCsvAgree() {
        TestDataSource json = new JsonTestDataSource();
        TestDataSource csv = new CsvTestDataSource();
        assertThat(csv.users()).isEqualTo(json.users()).hasSize(3);
        assertThat(csv.products()).isEqualTo(json.products()).hasSize(4);
        assertThat(json.users()).filteredOn(User::canSignIn).extracting(User::username)
                .containsExactly("standard_user", "visual_user");
    }

    @Test(description = "TC-502 the configured name picks the strategy, and an unknown name is refused")
    public void selection() {
        assertThat(TestDataSources.of("json", UNUSED)).isInstanceOf(JsonTestDataSource.class);
        assertThat(TestDataSources.of("csv", UNUSED)).isInstanceOf(CsvTestDataSource.class);
        assertThat(TestDataSources.of("api", UNUSED).name()).isEqualTo("api");
        assertThatThrownBy(() -> TestDataSources.of("xml", UNUSED))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("use json, csv or api");
    }
}
