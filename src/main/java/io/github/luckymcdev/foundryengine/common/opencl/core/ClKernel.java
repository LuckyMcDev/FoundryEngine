package io.github.luckymcdev.foundryengine.common.opencl.core;

/**
 * An OpenCl Kernel.
 */
public class ClKernel {
    private final long id;

    public ClKernel(String name, ClProgram program) {
        this.id = program.createKernel(name);
    }

    public long get() {
        return id;
    }
}