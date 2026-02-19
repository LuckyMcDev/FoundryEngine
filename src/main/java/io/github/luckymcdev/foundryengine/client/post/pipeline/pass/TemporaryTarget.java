package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

/**
 * Declares a named temporary framebuffer for a
 * {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline}.
 *
 * <p>This mirrors Minecraft's {@code PostChain} {@code <target name="…"/>} entries.
 * Pipelines list their required temporaries in their constructor via
 * {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline#addTarget},
 * and the {@code PostProcessManager} allocates (and auto-resizes) one
 * {@link io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer} per
 * unique name per pipeline on every frame.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * public class BloomPipeline extends PostProcessPipeline {
 *     public BloomPipeline(Identifier name, PostProcessPipelinePass... passes) {
 *         super(name, passes);
 *         addTarget(new TemporaryTarget("blur_h"));
 *         addTarget(new TemporaryTarget("blur_v"));
 *     }
 * }
 * }</pre>
 *
 * @param name the unique name used in {@link TargetRef#of(String)} to refer to this buffer
 */
public record TemporaryTarget(String name) {

    public TemporaryTarget {
        if (TargetRef.MAIN_NAME.equalsIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "\"main\" is a reserved target name – choose a different name for your TemporaryTarget.");
        }
    }

    /** Convenience factory: {@code TemporaryTarget.named("blur_ping")} */
    public static TemporaryTarget named(String name) {
        return new TemporaryTarget(name);
    }
}