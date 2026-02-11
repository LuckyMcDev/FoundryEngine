package io.github.luckymcdev.common.opencl.buffer;

import io.github.luckymcdev.common.opencl.core.ClDispatch;

import java.nio.FloatBuffer;

public class ClBuffer implements AutoCloseable {
    private final long id;
    private final ClDispatch dispatch;
    private boolean released = false;

    public ClBuffer(long id, ClDispatch dispatch) {
        this.id = id;
        this.dispatch = dispatch;
    }

    public long getId() {
        return id;
    }

    public void read( FloatBuffer dest) {
        dispatch.readBuffer(id, true, 0, dest);
    }

    public void write(FloatBuffer src) {
        dispatch.writeBuffer(id, true, 0, src);
    }

    public void release() {
        if (!released) {
            dispatch.releaseBuffer(id);
            released = true;
        }
    }

    @Override
    public void close() {
        release();
    }
}
