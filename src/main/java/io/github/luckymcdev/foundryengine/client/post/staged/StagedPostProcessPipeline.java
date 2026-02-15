package io.github.luckymcdev.foundryengine.client.post.staged;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.post.PostProcessPipeline;

public class StagedPostProcessPipeline extends PostProcessPipeline {
    private final PostProcessStage stage;

    public StagedPostProcessPipeline(PostProcessStage stage, ShaderProgram program) {
        super(program);
        this.stage = stage;
    }

    public PostProcessStage getStage() {
        return stage;
    }
}