package de.luckymcdev.foundryengine.common.util;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.Arrays;

public class UnsafeByteArrayOutputStream extends OutputStream implements AutoCloseable {
    private byte[] buf;
    private int index;

    public UnsafeByteArrayOutputStream() {
        this(128);
    }

    public UnsafeByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        this.buf = new byte[size];
    }

    public byte[] getBytes() {
        return Arrays.copyOf(this.buf, this.index);
    }

    private void ensureCapacity(int minCapacity) {
        int length = this.buf.length;
        if (minCapacity > length) {
            int size = Math.max(minCapacity, length / 2 + length);
            this.buf = Arrays.copyOf(this.buf, size);
        }
    }

    @Override
    public void write(int i) {
        this.ensureCapacity(this.index + 1);
        this.buf[this.index++] = (byte) i;
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) {
        if (off < 0 || off > b.length || len < 0 || off + len - b.length > 0) {
            throw new IndexOutOfBoundsException();
        }
        this.ensureCapacity(this.index + len);
        System.arraycopy(b, off, this.buf, this.index, len);
        this.index += len;
    }
}
