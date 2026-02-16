package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import net.minecraft.resources.Identifier;

public record PostProcessPipelinePass(Identifier name, Shader... shaders) {
}
