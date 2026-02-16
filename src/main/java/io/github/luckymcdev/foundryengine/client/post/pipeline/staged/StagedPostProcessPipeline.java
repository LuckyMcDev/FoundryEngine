package io.github.luckymcdev.foundryengine.client.post.pipeline.staged;

import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import net.minecraft.resources.Identifier;

public class StagedPostProcessPipeline extends PostProcessPipeline {
    private PostProcessStage stage;

    public StagedPostProcessPipeline(Identifier name, PostProcessStage stage, PostProcessPipelinePass... passes) {
        super(name, passes);
        this.stage = stage;
    }

    public PostProcessStage getStage() {
        return stage;
    }

    public void setStage(PostProcessStage stage) {
        this.stage = stage;
    }
}