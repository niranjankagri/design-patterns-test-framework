package com.niranjankagri.patterns.api;

import com.niranjankagri.patterns.config.TestConfig;
import com.niranjankagri.patterns.data.JsonTestDataSource;
import com.niranjankagri.patterns.data.TestDataSource;
import com.niranjankagri.patterns.data.TestDataSources;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The api test-data strategy reads from the running shop, so it is tested here and not with the unit tests. */
public class ApiDataSourceTest {

    @Test(description = "TC-503 the api data source returns the same users and products as the files")
    public void apiMatchesFiles() {
        TestDataSource api = TestDataSources.of("api", TestConfig.load().appUrl());
        TestDataSource json = new JsonTestDataSource();
        assertThat(api.users()).isEqualTo(json.users());
        assertThat(api.products()).isEqualTo(json.products());
    }
}
