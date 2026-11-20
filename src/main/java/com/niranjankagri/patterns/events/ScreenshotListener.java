package com.niranjankagri.patterns.events;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/** Saves the screenshots taken after failures as {@code screenshots/<test>.png}. */
public final class ScreenshotListener implements TestEventListener {

    @Override
    public void onEvent(TestEvent e) {
        if (e.kind() != TestEvent.Kind.SCREENSHOT || e.attachment().length == 0) {
            return;
        }
        String file = e.test().replaceAll("[^A-Za-z0-9._-]+", "_") + ".png";
        try {
            Files.write(OutputDir.resolve("screenshots", file), e.attachment());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
