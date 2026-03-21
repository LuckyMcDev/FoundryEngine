package de.luckymcdev.foundryengine.common.util;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {
    private final InputStream inputStream;
    private int read;

    public CountingInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        int read = this.inputStream.read();
        if (read != -1) {
            this.read++;
        }
        return read;
    }

    public int bytes() {
        return this.read;
    }
}
