package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import net.minecraft.resources.Identifier;

/**
 * Describes a single rendering pass inside a
 * {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline}.
 *
 * <p>Each pass declares which named {@link TargetRef} it reads from ({@code input}) and
 * which it writes to ({@code output}).  The {@code PostProcessManager} resolves these
 * names to real framebuffers at render time.  Any name other than {@code "main"} must
 * correspond to a {@link TemporaryTarget} registered on the owning pipeline.</p>
 *
 * <h3>Common patterns</h3>
 * <pre>{@code
 * // Simple single-pass: read main, write back to main
 * new PostProcessPipelinePass(id("grayscale"), TargetRef.MAIN, TargetRef.MAIN, vert, frag)
 *
 * // Two-pass blur using named temps
 * new PostProcessPipelinePass(id("blur_h"), TargetRef.MAIN,          TargetRef.of("blur_h"), vert, hBlur)
 * new PostProcessPipelinePass(id("blur_v"), TargetRef.of("blur_h"),  TargetRef.MAIN,          vert, vBlur)
 *
 * // Three-stage pipeline
 * new PostProcessPipelinePass(id("pass_a"), TargetRef.MAIN,          TargetRef.of("ping"), vert, fA)
 * new PostProcessPipelinePass(id("pass_b"), TargetRef.of("ping"),    TargetRef.of("pong"), vert, fB)
 * new PostProcessPipelinePass(id("pass_c"), TargetRef.of("pong"),    TargetRef.MAIN,       vert, fC)
 * }</pre>
 *
 * <p>When {@code output} is {@link TargetRef#MAIN} the manager renders into a pipeline-local
 * intermediate buffer and blits it back into Minecraft's main framebuffer after the draw call.</p>
 */
public record PostProcessPipelinePass(
        Identifier name,
        TargetRef  input,
        TargetRef  output,
        Shader...  shaders
) {
    /**
     * Convenience constructor: reads from {@link TargetRef#MAIN} and writes back to
     * {@link TargetRef#MAIN} (the typical single-pass setup).
     */
    public PostProcessPipelinePass(Identifier name, Shader... shaders) {
        this(name, TargetRef.MAIN, TargetRef.MAIN, shaders);
    }
}