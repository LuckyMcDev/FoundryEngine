package io.github.luckymcdev.foundryengine.client.editor.panels;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.post.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;

import java.util.List;
import java.util.Map;

/**
 * The Post Process Panel
 */
public class PostProcessPanel extends Panel {
    /**
     * The constant INSTANCE.
     */
    public static final PostProcessPanel INSTANCE = new PostProcessPanel();

    private PostProcessPanel() {
        super(Commons.id("post_process_panel"), "Post Processing Panel");
    }

    @Override
    public void content() {
        ImGui.text("Enable and Disable Pipelines right here");
        ImGui.separator();

        // Non-Staged Pipelines Section
        if (ImGui.collapsingHeader("Non-Staged Pipelines")) {
            List<PostProcessPipeline> allPipelines = Instances.getPostProcessManager().getPipelines();
            List<PostProcessPipeline> enabledPipelines = Instances.getPostProcessManager().getEnabledPipelines();

            if (allPipelines.isEmpty()) {
                ImGui.textDisabled("No non-staged pipelines registered");
            } else {
                for (PostProcessPipeline pipeline : allPipelines) {
                    renderPipelineControls(pipeline, enabledPipelines.contains(pipeline));
                }
            }
        }

        ImGui.separator();

        // Staged Pipelines Section
        if (ImGui.collapsingHeader("Staged Pipelines")) {
            List<StagedPostProcessPipeline> allStagedPipelines = Instances.getPostProcessManager().getStagedPipelines();
            List<StagedPostProcessPipeline> enabledStagedPipelines = Instances.getPostProcessManager().getEnabledStagedPipelines();
            Map<PostProcessStage, List<StagedPostProcessPipeline>> pipelinesByStage = Instances.getPostProcessManager().getPipelinesByStage();

            if (allStagedPipelines.isEmpty()) {
                ImGui.textDisabled("No staged pipelines registered");
            } else {
                // Group by stage
                for (PostProcessStage stage : PostProcessStage.values()) {
                    List<StagedPostProcessPipeline> stagePipelines = pipelinesByStage.get(stage);
                    boolean hasEnabledInStage = stagePipelines != null && !stagePipelines.isEmpty();

                    // Count total pipelines for this stage
                    int totalInStage = 0;
                    for (StagedPostProcessPipeline pipeline : allStagedPipelines) {
                        if (pipeline.getStage() == stage) {
                            totalInStage++;
                        }
                    }

                    if (totalInStage > 0) {
                        String stageHeader = stage.name() + " (" + (stagePipelines != null ? stagePipelines.size() : 0) + "/" + totalInStage + " enabled)";

                        if (ImGui.treeNode(stageHeader)) {
                            for (StagedPostProcessPipeline pipeline : allStagedPipelines) {
                                if (pipeline.getStage() == stage) {
                                    renderStagedPipelineControls(pipeline, enabledStagedPipelines.contains(pipeline));
                                }
                            }
                            ImGui.treePop();
                        }
                    }
                }
            }
        }
    }

    private void renderPipelineControls(PostProcessPipeline pipeline, boolean isEnabled) {
        ImGui.pushID(pipeline.getProgram().getId().toString());

        String id = pipeline.getProgram().getId().toString();
        ImGui.text("Pipeline: " + id);
        ImGui.sameLine();

        if (!isEnabled) {
            if (ImGui.button("Enable")) {
                Instances.getPostProcessManager().enablePipeline(pipeline);
            }
        } else {
            if (ImGui.button("Disable")) {
                Instances.getPostProcessManager().disablePipeline(pipeline);
            }
        }

        ImGui.separator();
        ImGui.popID();
    }

    private void renderStagedPipelineControls(StagedPostProcessPipeline pipeline, boolean isEnabled) {
        ImGui.pushID(pipeline.getProgram().getId().toString() + "_staged");

        String id = pipeline.getProgram().getId().toString();
        ImGui.text("Pipeline: " + id);
        ImGui.sameLine();

        if (!isEnabled) {
            if (ImGui.button("Enable")) {
                Instances.getPostProcessManager().enablePipeline(pipeline);
            }
        } else {
            if (ImGui.button("Disable")) {
                Instances.getPostProcessManager().disablePipeline(pipeline);
            }
        }

        ImGui.separator();
        ImGui.popID();
    }
}