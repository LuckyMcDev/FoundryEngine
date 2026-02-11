package io.github.luckymcdev.common.opencl.task;

import io.github.luckymcdev.common.opencl.buffer.ClBuffer;
import io.github.luckymcdev.common.opencl.core.ClDispatch;
import io.github.luckymcdev.common.opencl.core.ClKernel;
import io.github.luckymcdev.common.opencl.core.ClProgram;
import io.github.luckymcdev.common.opencl.core.OpenClContext;
import net.minecraft.resources.ResourceLocation;
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
import static org.lwjgl.opencl.CL12.clGetKernelArgInfo;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memASCII;

public class ClComputeTask {
    private final Map<String, Integer> argMap = new HashMap<>();
    private final OpenClContext context;
    private final ClDispatch dispatch;
    private final ClProgram program;
    private final ClKernel kernel;
    private final List<ClBuffer> buffers;
    private int nextArgIndex = 0;

    private ClComputeTask(OpenClContext context, ClDispatch dispatch,
                          ClProgram program, ClKernel kernel) {
        this.context = context;
        this.dispatch = dispatch;
        this.program = program;
        this.kernel = kernel;
        this.buffers = new ArrayList<>();
    }

    public static Builder create(ResourceLocation kernelFile, String kernelName) {
        return new Builder(kernelFile, kernelName);
    }

    public ClBuffer addOutputBuffer(int numElements) {
        long bufferId = dispatch.createBuffer(
                CL_MEM_WRITE_ONLY, numElements * Float.BYTES
        );
        ClBuffer buffer = new ClBuffer(bufferId, dispatch);
        buffers.add(buffer);
        dispatch.setKernelArg(kernel, nextArgIndex++, bufferId);
        return buffer;
    }

    public ClBuffer addInputBuffer(FloatBuffer data) {
        long bufferId = dispatch.createBuffer(
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, data
        );
        ClBuffer buffer = new ClBuffer(bufferId, dispatch);
        buffers.add(buffer);
        dispatch.setKernelArg(kernel, nextArgIndex++, bufferId);
        return buffer;
    }

    public <V> ClComputeTask setArg(String name, V value) {
        Integer index = argMap.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Kernel argument '" + name + "' not found!");
        }

        try (MemoryStack stack = stackPush()) {
            if (value instanceof Integer i) {
                clSetKernelArg(kernel.get(), index, stack.ints(i));
            } else if (value instanceof Float f) {
                clSetKernelArg(kernel.get(), index, stack.floats(f));
            } else if (value instanceof ClBuffer buf) {
                clSetKernelArg(kernel.get(), index, stack.pointers(buf.getId()));
            } else if (value instanceof Long l) {
                clSetKernelArg(kernel.get(), index, stack.longs(l));
            } else {
                throw new UnsupportedOperationException("Unsupported arg type: " + value.getClass());
            }
        }
        return this;
    }

    public ClComputeTask setArg(int index, int value) {
        try (MemoryStack stack = stackPush()) {
            clSetKernelArg1i(kernel.get(), index, value);
            return this;
        }
    }

    public ClComputeTask setArg(int index, float value) {
        try (MemoryStack stack = stackPush()) {
            clSetKernelArg1f(kernel.get(), index, value);
            return this;
        }
    }

    public ClComputeTask setArg(int index, long value) {
        try (MemoryStack stack = stackPush()) {
            clSetKernelArg1l(kernel.get(), index, value);
            return this;
        }
    }

    private void reflectArgs(long kernelId) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer numArgsBuf = stack.mallocInt(1);
            clGetKernelInfo(kernelId, CL_KERNEL_NUM_ARGS, numArgsBuf, null);
            int numArgs = numArgsBuf.get(0);

            for (int i = 0; i < numArgs; i++) {
                PointerBuffer sizeRet = stack.mallocPointer(1);
                clGetKernelArgInfo(kernelId, i, CL_KERNEL_ARG_NAME, (ByteBuffer) null, sizeRet);

                ByteBuffer nameBuf = stack.malloc((int) sizeRet.get(0));
                clGetKernelArgInfo(kernelId, i, CL_KERNEL_ARG_NAME, nameBuf, null);

                String argName = memASCII(nameBuf, (int) sizeRet.get(0) - 1);
                argMap.put(argName, i);
            }
        }
    }

    public void execute(long... workSize) {
        dispatch.enqueueKernel(kernel, workSize.length, workSize);
    }

    public void cleanup() {
        buffers.forEach(ClBuffer::release);
        buffers.clear();
        program.cleanup();
        dispatch.cleanup();
        context.cleanup();
    }

    public static class Builder {
        private final ResourceLocation kernelFile;
        private final String kernelName;
        private boolean printInfo = false;

        private Builder(ResourceLocation kernelFile, String kernelName) {
            this.kernelFile = kernelFile;
            this.kernelName = kernelName;
        }

        public Builder withDebugInfo() {
            this.printInfo = true;
            return this;
        }

        public ClComputeTask build() {
            OpenClContext context = new OpenClContext(printInfo);
            ClDispatch dispatch = new ClDispatch(context);
            ClProgram program = new ClProgram(context, kernelFile);
            ClKernel kernel = new ClKernel(kernelName, program);
            return new ClComputeTask(context, dispatch, program, kernel);
        }
    }
}
