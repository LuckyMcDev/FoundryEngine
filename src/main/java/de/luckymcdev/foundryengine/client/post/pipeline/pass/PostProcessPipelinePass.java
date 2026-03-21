package de.luckymcdev.foundryengine.client.post.pipeline.pass;

import de.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import net.minecraft.resources.Identifier;

/**
 * A Record representing a Single Pipeline Pass.
 * A pipeline Pass is when you have an input, shaders and an output.
 * The shaders run on the input and put their result into the output.
 * @param name the name of this Pass.
 * @param input the {@link TargetRef} input.
 * @param output the {@link TargetRef} output.
 * @param shaders n ammount of Shaders.
 */
public record PostProcessPipelinePass(Identifier name, TargetRef input, TargetRef output, Shader... shaders) {
    /**
     * Convenience constructor: reads from {@link TargetRef#MAIN} and writes back to {@link TargetRef#MAIN}.
     */
    public PostProcessPipelinePass(Identifier name, Shader... shaders) {
        this(name, TargetRef.MAIN, TargetRef.MAIN, shaders);
    }
}