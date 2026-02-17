package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import net.minecraft.resources.Identifier;

/**
 * Describes a single rendering pass inside a {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline}.
 *
 * <p>Each pass explicitly declares which named {@link PassTarget} it reads from ({@code input})
 * and which it writes to ({@code output}).  The {@code PostProcessManager} resolves these names
 * to real {@link io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer} objects
 * at render time, creating and resizing them as necessary.</p>
 *
 * <h3>Common patterns</h3>
 * <pre>{@code
 * // Simple single-pass: read main, write back to main via a temp buffer
 * new PostProcessPipelinePass(id("bloom"), PassTarget.MAIN, PassTarget.MAIN, vertShader, fragShader)
 *
 * // Two-pass blur: H-blur to PING, then V-blur back to MAIN
 * new PostProcessPipelinePass(id("blur_h"), PassTarget.MAIN, PassTarget.PING, v, hBlur)
 * new PostProcessPipelinePass(id("blur_v"), PassTarget.PING, PassTarget.MAIN, v, vBlur)
 *
 * // Three-stage pipeline using all scratch buffers
 * new PostProcessPipelinePass(id("pass_a"), PassTarget.MAIN, PassTarget.PING,  v, fA)
 * new PostProcessPipelinePass(id("pass_b"), PassTarget.PING, PassTarget.PONG,  v, fB)
 * new PostProcessPipelinePass(id("pass_c"), PassTarget.PONG, PassTarget.MAIN,  v, fC)
 * }</pre>
 *
 * <p>When {@code output} is {@link PassTarget#MAIN} the manager performs a blit from the
 * intermediate scratch buffer into Minecraft's main framebuffer after the draw call, so
 * the result is visible in the final frame.</p>
 */
public record PostProcessPipelinePass(
        Identifier name,
        PassTarget input,
        PassTarget output,
        Shader... shaders
) {
    /**
     * Convenience constructor for a pass that reads from {@link PassTarget#MAIN} and routes
     * back to {@link PassTarget#MAIN} (the typical single-pass use-case).
     */
    public PostProcessPipelinePass(Identifier name, Shader... shaders) {
        this(name, PassTarget.MAIN, PassTarget.MAIN, shaders);
    }
}