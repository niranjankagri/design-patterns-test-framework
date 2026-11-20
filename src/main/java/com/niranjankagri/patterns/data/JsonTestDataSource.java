package com.niranjankagri.patterns.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Reads test data from {@code data/users.json} and {@code data/products.json} on the classpath. */
public final class JsonTestDataSource implements TestDataSource {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public String name() {
        return "json";
    }

    @Override
    public List<User> users() {
        return read("data/users.json", new TypeReference<>() { });
    }

    @Override
    public List<Product> products() {
        return read("data/products.json", new TypeReference<>() { });
    }

    private static <T> List<T> read(String resource, TypeReference<List<T>> type) {
        try (InputStream in = Classpath.open(resource)) {
            return List.copyOf(JSON.readValue(in, type));
        } catch (IOException e) {
            throw new IllegalStateException(resource + " is not valid test data", e);
        }
    }
}
