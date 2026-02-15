package io.github.luckymcdev.common.opencl.task;

import io.github.luckymcdev.common.opencl.ClDispatch;
import io.github.luckymcdev.common.opencl.buffer.ClBuffer;
import io.github.luckymcdev.common.opencl.core.ClCommandQueue;
import io.github.luckymcdev.common.opencl.core.ClKernel;
import io.github.luckymcdev.common.opencl.core.ClProgram;
import io.github.luckymcdev.common.opencl.core.OpenClContext;
import net.minecraft.resources.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL12.CL_KERNEL_ARG_NAME;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memASCII;

public class ClComputeTask {
    private final Map<String, Integer> argMap = new HashMap<>();
    private final OpenClContext context;
    private final ClCommandQueue commandQueue;
    private final ClProgram program;
    private final ClKernel kernel;
    private final List<ClBuffer> buffers;
    private int nextArgIndex = 0;

    private ClComputeTask(OpenClContext context, ClCommandQueue commandQueue,
                          ClProgram program, ClKernel kernel) {
        this.context = context;
        this.commandQueue = commandQueue;
        this.program = program;
        this.kernel = kernel;
        this.buffers = new ArrayList<>();
    }

    public static Builder create(Identifier kernelFile, String kernelName) {
        return new Builder(kernelFile, kernelName);
    }

    public ClBuffer addOutputBuffer(int numElements) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);
            long bufferId = ClDispatch.createBuffer(
                    context.getContext(), CL_MEM_WRITE_ONLY, numElements * Float.BYTES, errcode
            );
            OpenClContext.checkCLError(errcode.get(0), "Failed to create output buffer");

            ClBuffer buffer = new ClBuffer(bufferId, commandQueue);
            buffers.add(buffer);
            ClDispatch.setKernelArg1p(kernel.get(), nextArgIndex++, bufferId);
            return buffer;
        }
    }

    public ClBuffer addInputBuffer(FloatBuffer data) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);
            long bufferId = ClDispatch.createBuffer(
                    context.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, data, errcode
            );
            OpenClContext.checkCLError(errcode.get(0), "Failed to create input buffer");

            ClBuffer buffer = new ClBuffer(bufferId, commandQueue);
            buffers.add(buffer);
            ClDispatch.setKernelArg1p(kernel.get(), nextArgIndex++, bufferId);
            return buffer;
        }
    }

    public <V> ClComputeTask setArg(String name, V value) {
        Integer index = argMap.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Kernel argument '" + name + "' not found!");
        }

        try (MemoryStack stack = stackPush()) {
            switch (value) {
                case Integer i -> ClDispatch.setKernelArg(kernel.get(), index, stack.ints(i));
                case Float f -> ClDispatch.setKernelArg(kernel.get(), index, stack.floats(f));
                case ClBuffer buf -> ClDispatch.setKernelArg(kernel.get(), index, stack.pointers(buf.getId()));
                case Long l -> ClDispatch.setKernelArg(kernel.get(), index, stack.longs(l));
                default -> throw new UnsupportedOperationException("Unsupported arg type: " + value.getClass());
            }
        }
        return this;
    }

    public ClComputeTask setArg(int index, int value) {
        ClDispatch.setKernelArg1i(kernel.get(), index, value);
        return this;
    }

    public ClComputeTask setArg(int index, float value) {
        ClDispatch.setKernelArg1f(kernel.get(), index, value);
        return this;
    }

    public ClComputeTask setArg(int index, long value) {
        ClDispatch.setKernelArg1l(kernel.get(), index, value);
        return this;
    }

    private void reflectArgs(long kernelId) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer numArgsBuf = stack.mallocInt(1);
            ClDispatch.getKernelInfo(kernelId, CL_KERNEL_NUM_ARGS, numArgsBuf, null);
            int numArgs = numArgsBuf.get(0);

            for (int i = 0; i < numArgs; i++) {
                PointerBuffer sizeRet = stack.mallocPointer(1);
                ClDispatch.getKernelArgInfo(kernelId, i, CL_KERNEL_ARG_NAME, (ByteBuffer) null, sizeRet);

                ByteBuffer nameBuf = stack.malloc((int) sizeRet.get(0));
                ClDispatch.getKernelArgInfo(kernelId, i, CL_KERNEL_ARG_NAME, nameBuf, null);

                String argName = memASCII(nameBuf, (int) sizeRet.get(0) - 1);
                argMap.put(argName, i);
            }
        }
    }

    public void execute(long... workSize) {
        commandQueue.enqueueKernel(kernel, workSize.length, workSize);
    }

    public ClCommandQueue getCommandQueue() {
        return commandQueue;
    }

    public ClProgram getProgram() {
        return program;
    }

    public ClKernel getKernel() {
        return kernel;
    }

    public List<ClBuffer> getBuffers() {
        return buffers;
    }

    public OpenClContext getContext() {
        return context;
    }

    public void cleanup() {
        buffers.forEach(ClBuffer::release);
        buffers.clear();
        program.cleanup();
        commandQueue.cleanup();
        context.cleanup();
    }

    public static class Builder {
        private final Identifier kernelFile;
        private final String kernelName;
        private boolean printInfo = false;

        private Builder(Identifier kernelFile, String kernelName) {
            this.kernelFile = kernelFile;
            this.kernelName = kernelName;
        }

        public Builder withDebugInfo() {
            this.printInfo = true;
            return this;
        }

        public ClComputeTask build() {
            OpenClContext context = new OpenClContext(printInfo);
            ClCommandQueue dispatch = new ClCommandQueue(context);
            ClProgram program = new ClProgram(context, kernelFile);
            ClKernel kernel = new ClKernel(kernelName, program);
            return new ClComputeTask(context, dispatch, program, kernel);
        }
    }
}