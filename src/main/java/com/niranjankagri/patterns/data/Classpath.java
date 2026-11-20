package com.niranjankagri.patterns.data;

import java.io.InputStream;

/** Opens test-data files on the classpath with a clear error when one is missing. */
final class Classpath {

    private Classpath() {
    }

    static InputStream open(String resource) {
        InputStream in = Classpath.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalStateException("Test data file not found on the classpath: " + resource);
        }
        return in;
    }
}
