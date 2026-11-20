package com.niranjankagri.patterns.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Where the listeners write their files: {@code -Devents.dir}, default {@code target/test-events}. */
final class OutputDir {

    private OutputDir() {
    }

    static Path resolve(String... parts) {
        Path dir = Path.of(System.getProperty("events.dir", "target/test-events"));
        Path file = dir.resolve(String.join("/", parts));
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }
}
