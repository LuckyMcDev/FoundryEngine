package com.example.post

import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage
import io.github.luckymcdev.foundryengine.common.Common
import net.minecraft.resources.Identifier


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