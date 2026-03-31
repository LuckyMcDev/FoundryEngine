package com.example.post

import de.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType
import de.luckymcdev.foundryengine.client.opengl.shaders.Shader
import de.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource
import de.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline
import de.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass
import de.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef
import de.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage
import de.luckymcdev.foundryengine.common.Common
import net.minecraft.resources.Identifier

/**
 * This is an example of a custom post-processing pipeline.
 * This is an advanced Topic. You should be familiar with shaders and the rendering pipeline to understand this.
 */
class TestPostProcessPipeline extends PostProcessPipeline {

    @Override
    Identifier getName() {
        return Identifier.fromNamespaceAndPath("testbundle", "post_test_pipeline")
    }

    @Override
    PostProcessStage getInitialStage() {
        return PostProcessStage.FINAL
    }

    @Override
    List<PostProcessPipelinePass> getPasses() {
        return List.of(
            new PostProcessPipelinePass(
                    Identifier.fromNamespaceAndPath("testbundle", "post_test_pass"),
                    TargetRef.MAIN, TargetRef.MAIN,
                    new Shader(ExtendedShaderType.VERTEX,
                            new ShaderSource(
                                    Identifier.fromNamespaceAndPath("testbundle", "post_test_vert"),
                                    Common.id("shaders/vert.vsh")
                            )),
                    new Shader(ExtendedShaderType.FRAGMENT,
                            new ShaderSource(
                                    Identifier.fromNamespaceAndPath("testbundle","post_test_frag"),
                                    Identifier.fromNamespaceAndPath("testbundle", "shaders/post/test_post.fsh")
                            ))
            )
        )
    }
}