package com.niranjankagri.patterns.data;

import com.niranjankagri.patterns.model.Product;
import com.niranjankagri.patterns.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * Reads test data from {@code data/users.csv} and {@code data/products.csv} on the classpath: a header line, then one
 * record per line, comma-separated (the data has no commas, so no quoting is needed).
 */
public final class CsvTestDataSource implements TestDataSource {

    @Override
    public String name() {
        return "csv";
    }

    @Override
    public List<User> users() {
        return read("data/users.csv", 3, f -> new User(f[0], f[1], f[2]));
    }

    @Override
    public List<Product> products() {
        return read("data/products.csv", 3, f -> new Product(f[0], f[1], Integer.parseInt(f[2])));
    }

    private static <T> List<T> read(String resource, int columns, Function<String[], T> toRecord) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(Classpath.open(resource), StandardCharsets.UTF_8))) {
            return in.lines().skip(1).filter(line -> !line.isBlank()).map(line -> {
                String[] fields = line.split(",", -1);
                if (fields.length != columns) {
                    throw new IllegalStateException(resource + ": expected " + columns + " columns in '" + line + "'");
                }
                return toRecord.apply(fields);
            }).toList();
        } catch (IOException e) {
            throw new IllegalStateException(resource + " could not be read", e);
        }
    }
}
