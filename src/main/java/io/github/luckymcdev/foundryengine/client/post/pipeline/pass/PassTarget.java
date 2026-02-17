package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

/**
 * Represents a named framebuffer slot used as the input or output of a pipeline pass.
 *
 * <ul>
 *   <li>{@link #MAIN}  – the engine's main render target (Minecraft's framebuffer).</li>
 *   <li>{@link #PING}  – the first manager-owned scratch buffer.</li>
 *   <li>{@link #PONG}  – the second manager-owned scratch buffer, enabling ping-pong patterns.</li>
 *   <li>{@link #TEMP}  – a third scratch buffer for more complex multi-pass pipelines.</li>
 * </ul>
 *
 * The {@code PostProcessManager} owns all non-{@link #MAIN} buffers and automatically
 * creates or resizes them to match the main render target on every frame.
 */
public enum PassTarget {
    /**
     * The engine's primary render target. When used as an output the manager will
     * blit the previous pass's output buffer back into the main framebuffer.
     */
    MAIN,

    /** First scratch framebuffer – always available, manager-owned. */
    PING,

    /** Second scratch framebuffer – enables ping-pong multi-pass patterns. */
    PONG,

    /** Third scratch framebuffer – for pipelines that need a third intermediate. */
    TEMP;

    /** Returns {@code true} for targets whose framebuffers are managed by {@code PostProcessManager}. */
    public boolean isManagerOwned() {
        return this != MAIN;
    }
}