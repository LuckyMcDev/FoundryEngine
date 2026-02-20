package io.github.luckymcdev.foundryengine.common.opencl.core;

import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.opencl.ClDispatch;
import net.minecraft.resources.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opencl.CL10.CL_PROGRAM_BUILD_LOG;
import static org.lwjgl.opencl.CL10.CL_SUCCESS;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A OpenCl Program.
 */
public class ClProgram {
    private final OpenClContext context;
    private final Map<String, Long> kernels;
    private long program;


    public ClProgram(OpenClContext context, Identifier sourceLoc) {
        this.context = context;
        this.kernels = new HashMap<>();
        String source = Commons.getRlSource(sourceLoc);
        buildProgram(source);
    }

    private void buildProgram(String source) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);

            // Create program
            program = ClDispatch.createProgramWithSource(context.getContext(), source, errcode);
            OpenClContext.checkCLError(errcode.get(0), "Failed to create program");

            // Build program
            int buildStatus = ClDispatch.buildProgram(program, context.getDevice(),
                    "-cl-kernel-arg-info", null, NULL);

            if (buildStatus != CL_SUCCESS) {
                // Print build log if compilation failed
                printBuildLog(stack);
                OpenClContext.checkCLError(buildStatus, "Failed to build program");
            }
        }
    }

    private void printBuildLog(MemoryStack stack) {
        PointerBuffer logSize = stack.mallocPointer(1);
        ClDispatch.getProgramBuildInfo(program, context.getDevice(), CL_PROGRAM_BUILD_LOG,
                null, logSize);

        int size = (int) logSize.get(0);
        if (size > 0) {
            PointerBuffer log = stack.mallocPointer(1);
            ClDispatch.getProgramBuildInfo(program, context.getDevice(), CL_PROGRAM_BUILD_LOG,
                    log, null);
            System.err.println("OpenCL Build Log:\n" + log.getStringUTF8(0));
        }
    }

    public long createKernel(String kernelName) {
        if (kernels.containsKey(kernelName)) {
            return kernels.get(kernelName);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode = stack.mallocInt(1);
            long kernel = ClDispatch.createKernel(program, kernelName, errcode);
            OpenClContext.checkCLError(errcode.get(0), "Failed to create kernel: " + kernelName);

            kernels.put(kernelName, kernel);
            return kernel;
        }
    }

    public long getKernel(String kernelName) {
        return kernels.get(kernelName);
    }

    public void cleanup() {
        for (long kernel : kernels.values()) {
            ClDispatch.releaseKernel(kernel);
        }
        kernels.clear();

        if (program != 0) {
            ClDispatch.releaseProgram(program);
            program = 0;
        }
    }
}