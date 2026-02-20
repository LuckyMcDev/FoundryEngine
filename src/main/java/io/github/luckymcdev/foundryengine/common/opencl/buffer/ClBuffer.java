package io.github.luckymcdev.foundryengine.common.opencl.buffer;

import io.github.luckymcdev.foundryengine.common.opencl.ClDispatch;
import io.github.luckymcdev.foundryengine.common.opencl.core.ClCommandQueue;

import java.nio.FloatBuffer;

/**
 * A ClBuffer represents a memory object in OpenCL memory space.
 */
public class ClBuffer implements AutoCloseable {
    private final long id;
    private final ClCommandQueue dispatch;
    private boolean released = false;

    /**
     * Creates a new ClBuffer with a specific ID and command queue.
     *
     * @param id       the OpenCL memory object ID
     * @param dispatch the command queue used for operations
     */
    public ClBuffer(long id, ClCommandQueue dispatch) {
        this.id = id;
        this.dispatch = dispatch;
    }

    /**
     * Gets the OpenCL memory object ID.
     * @return the long ID
     */
    public long getId() {
        return id;
    }

    /**
     * Reads data from the OpenCL buffer into a FloatBuffer.
     * @param dest the destination FloatBuffer
     */
    public void read(FloatBuffer dest) {
        dispatch.readBuffer(id, true, 0, dest);
    }

    /**
     * Writes data from a FloatBuffer into the OpenCL buffer.
     * @param src the source FloatBuffer
     */
    public void write(FloatBuffer src) {
        dispatch.writeBuffer(id, true, 0, src);
    }

    /**
     * Releases the OpenCL memory object if it hasn't been released yet.
     */
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