package io.github.luckymcdev.foundryengine.common.opencl.buffer;

import io.github.luckymcdev.foundryengine.common.opencl.ClDispatch;
import io.github.luckymcdev.foundryengine.common.opencl.core.ClCommandQueue;

import java.nio.FloatBuffer;

/**
 * A ClBuffer, which is a list of Commands openCl should run
 */
public class ClBuffer implements AutoCloseable {
    private final long id;
    private final ClCommandQueue dispatch;
    private boolean released = false;

    public ClBuffer(long id, ClCommandQueue dispatch) {
        this.id = id;
        this.dispatch = dispatch;
    }

    public long getId() {
        return id;
    }

    public void read(FloatBuffer dest) {
        dispatch.readBuffer(id, true, 0, dest);
    }

    public void write(FloatBuffer src) {
        dispatch.writeBuffer(id, true, 0, src);
    }

    public void release() {
        if (!released) {
            ClDispatch.releaseMemObject(id);
            released = true;
        }
    }

    @Override
    public void close() {
        release();
    }
}