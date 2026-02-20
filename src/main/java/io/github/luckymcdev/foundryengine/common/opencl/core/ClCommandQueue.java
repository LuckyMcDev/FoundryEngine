package io.github.luckymcdev.foundryengine.common.opencl.core;

import io.github.luckymcdev.foundryengine.common.opencl.ClDispatch;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * A Queue of {@link ClKernel} / Commands OpenCl should run.
 */
public class ClCommandQueue {
    private final OpenClContext context;
    private long commandQueue;

    public ClCommandQueue(OpenClContext context) {
        this.context = context;
        createCommandQueue();
    }

    private void createCommandQueue() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errorCode = stack.mallocInt(1);
            commandQueue = ClDispatch.createCommandQueue(context.getContext(),
                    context.getDevice(), 0, errorCode);
            OpenClContext.checkCLError(errorCode.get(0), "Failed to create command queue");
        }
    }

    public void enqueueKernel(ClKernel kernel, int workDim, long... globalWorkSize) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer globalWork = stack.mallocPointer(workDim);
            for (int i = 0; i < workDim; i++) {
                globalWork.put(i, globalWorkSize[i]);
            }

            int result = ClDispatch.enqueueNDRangeKernel(commandQueue, kernel.get(), workDim,
                    null, globalWork, null, null, null);
            OpenClContext.checkCLError(result, "Failed to enqueue kernel");
        }
    }

    public void readBuffer(long buffer, boolean blocking, long offset, FloatBuffer dest) {
        int result = ClDispatch.enqueueReadBuffer(commandQueue, buffer, blocking, offset,
                dest, null, null);
        OpenClContext.checkCLError(result, "Failed to read buffer");
    }

    public void writeBuffer(long buffer, boolean blocking, long offset, FloatBuffer src) {
        int result = ClDispatch.enqueueWriteBuffer(commandQueue, buffer, blocking, offset,
                src, null, null);
        OpenClContext.checkCLError(result, "Failed to write buffer");
    }

    public void finish() {
        ClDispatch.finish(commandQueue);
    }

    public void cleanup() {
        if (commandQueue != 0) {
            ClDispatch.releaseCommandQueue(commandQueue);
            commandQueue = 0;
        }
    }

    public long getCommandQueue() {
        return commandQueue;
    }
}