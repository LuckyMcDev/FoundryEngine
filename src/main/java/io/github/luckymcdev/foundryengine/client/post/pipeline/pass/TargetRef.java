package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer;

/**
 * A reference to a {@link FrameBuffer} / {@link RenderTarget}
 * for Rendering. Used in {@link PostProcessPipelinePass}
 */
public final class TargetRef {
    /**
     * Minecraft's Main Framebuffer. Reserved Name!
     */
    public static final String MAIN_NAME = "main";

    /**
     * Minecraft's Primary Target.
     */
    public static final TargetRef MAIN = new TargetRef(MAIN_NAME);

    private final String name;

    private TargetRef(String name) {
        this.name = name;
    }

    public static TargetRef named(String name) {
        return new TargetRef(name);
    }

    /**
     * Returns a temporary target ref for the given name.
     * <br> must NOT be {@link #MAIN_NAME}
     * @param name the name of the target ref.
     * @return the created Target Ref.
     */
    public static TargetRef of(String name) {
        if (MAIN_NAME.equalsIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Use TargetRef.MAIN instead of TargetRef.of(\"main\").");
        }
        return new TargetRef(name);
    }

    /**
     * Returns the raw name of this target.
     */
    public String name() {
        return name;
    }

    /**
     * Returns {@code true} if this reference points to the main render target.
     */
    public boolean isMain() {
        return MAIN_NAME.equals(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TargetRef r && name.equals(r.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}