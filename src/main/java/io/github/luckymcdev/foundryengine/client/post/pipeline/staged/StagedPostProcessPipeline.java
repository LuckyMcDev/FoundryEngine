package io.github.luckymcdev.foundryengine.client.post.pipeline.staged;

import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import net.minecraft.resources.Identifier;

/**
 * An extension to a {@link PostProcessPipeline} which renders at a specific {@link PostProcessStage}.
 */
public class StagedPostProcessPipeline extends PostProcessPipeline {
    private PostProcessStage stage;

    /**
     * {@inheritDoc}
     *
     * @param name   The unique identifier for this Post Process Pipeline.
     * @param stage  The stage at which this pipeline should render.
     * @param passes The passes for this PostProcessPipeline.
     */
    public StagedPostProcessPipeline(Identifier name, PostProcessStage stage, PostProcessPipelinePass... passes) {
        super(name, passes);
        this.stage = stage;
    }

    /**
     * Gets the stage at which this pipeline renders.
     *
     * @return The {@link PostProcessStage} of this pipeline.
     */
    public PostProcessStage getStage() {
        return stage;
    }

    /**
     * Sets the stage at which this pipeline renders.
     *
     * @param stage The new {@link PostProcessStage} for this pipeline.
     */
    public void setStage(PostProcessStage stage) {
        this.stage = stage;
    }
}