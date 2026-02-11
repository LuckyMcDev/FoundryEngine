package io.github.luckymcdev.common.opencl.core;

import io.github.luckymcdev.common.opencl.buffer.ClBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class ClDispatch {
    private final OpenClContext context;
    private long commandQueue;

    public ClDispatch(OpenClContext context) {
        this.context = context;
        createCommandQueue();
    }

    private void createCommandQueue() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);
            commandQueue = clCreateCommandQueue(context.getContext(),
                    context.getDevice(), 0, errcode);
            OpenClContext.checkCLError(errcode.get(0), "Failed to create command queue");
        }
    }

    public long createBuffer(int flags, FloatBuffer hostData) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);
            long buffer = clCreateBuffer(context.getContext(), flags, hostData, errcode);
            OpenClContext.checkCLError(errcode.get(0), "Failed to create buffer");
            return buffer;
        }
    }

    public long createBuffer(int flags, long size) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);
            long buffer = clCreateBuffer(context.getContext(), flags, size, errcode);
            OpenClContext.checkCLError(errcode.get(0), "Failed to create buffer");
            return buffer;
        }
    }

    public void setKernelArg(ClKernel kernel, int index, long buffer) {
        clSetKernelArg1p(kernel.get(), index, buffer);
    }

    public void enqueueKernel(ClKernel kernel, int workDim, long... globalWorkSize) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer globalWork = stack.mallocPointer(workDim);
            for (int i = 0; i < workDim; i++) {
                globalWork.put(i, globalWorkSize[i]);
            }

            int result = clEnqueueNDRangeKernel(commandQueue, kernel.get(), workDim,
                    null, globalWork, null, null, null);
            OpenClContext.checkCLError(result, "Failed to enqueue kernel");
        }
    }

    public void readBuffer(long buffer, boolean blocking, long offset, FloatBuffer dest) {
        int result = clEnqueueReadBuffer(commandQueue, buffer, blocking, offset,
                dest, null, null);
        OpenClContext.checkCLError(result, "Failed to read buffer");
    }

    public void writeBuffer(long buffer, boolean blocking, long offset, FloatBuffer src) {
        int result = clEnqueueWriteBuffer(commandQueue, buffer, blocking, offset,
                src, null, null);
        OpenClContext.checkCLError(result, "Failed to write buffer");
    }

    public ClBuffer createInputBuffer(FloatBuffer data) {
        long bufferId = createBuffer(CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, data);
        return new ClBuffer(bufferId, this);
    }

    public ClBuffer createOutputBuffer(int numElements) {
        long bufferId = createBuffer(CL_MEM_WRITE_ONLY, numElements * Float.BYTES);
        return new ClBuffer(bufferId, this);
    }

    public ClBuffer createReadWriteBuffer(FloatBuffer data) {
        long bufferId = createBuffer(CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, data);
        return new ClBuffer(bufferId, this);
    }

    public void finish() {
        clFinish(commandQueue);
    }

    public void releaseBuffer(long buffer) {
        clReleaseMemObject(buffer);
    }

    public void cleanup() {
        if (commandQueue != 0) {
            clReleaseCommandQueue(commandQueue);
            commandQueue = 0;
        }
    }
}