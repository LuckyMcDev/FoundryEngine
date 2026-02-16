package io.github.luckymcdev.foundryengine.client.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;

import java.util.List;
import java.util.Map;

public class PostProcessPanel extends Panel {
    public static final PostProcessPanel INSTANCE = new PostProcessPanel();

    private PostProcessPanel() {
        super(Commons.id("post_process_panel"), "Post Processing Panel");
    }

    @Override
    public void content() {
        ImGui.text("Post Processing Management");
        ImGui.separator();

        if (ImGui.collapsingHeader("Global Pipelines")) {
            renderNonStagedSection();
        }

        ImGui.separator();

        if (ImGui.collapsingHeader("Staged Pipelines", ImGuiTreeNodeFlags.DefaultOpen)) {
            var postProcessManager = Instances.getPostProcessManager();
            Map<PostProcessStage, List<StagedPostProcessPipeline>> pipelinesByStage = postProcessManager.getPipelinesByStage();

            for (PostProcessStage stage : PostProcessStage.values()) {
                List<StagedPostProcessPipeline> stagePipelines = pipelinesByStage.getOrDefault(stage, List.of());
                int stageCount = stagePipelines.size();

                if (ImGui.treeNode(stage.name() + " (" + stageCount + ")")) {
                    if (stageCount == 0) {
                        ImGui.textDisabled("  No pipelines in this stage");
                    } else {
                        for (StagedPostProcessPipeline pipeline : stagePipelines) {
                            renderStagedPipelineRow(pipeline);
                        }
                    }
                    ImGui.treePop();
                }
            }
        }
    }

    private void renderNonStagedSection() {
        List<PostProcessPipeline> allPipelines = Instances.getPostProcessManager().getPipelines();
        if (allPipelines.isEmpty()) {
            ImGui.textDisabled("No global pipelines registered");
            return;
        }

        for (PostProcessPipeline pipeline : allPipelines) {
            ImGui.pushID(pipeline.getName().toString());
            boolean enabled = pipeline.isEnabled();
            if (ImGui.checkbox("##enabled", enabled)) {
                if (enabled) pipeline.disable(); else pipeline.enable();
            }
            ImGui.sameLine();
            ImGui.text(pipeline.getName().toString());
            ImGui.popID();
        }
    }

    private void renderStagedPipelineRow(StagedPostProcessPipeline pipeline) {
        ImGui.pushID(pipeline.getName().toString() + "_row");

        boolean enabled = pipeline.isEnabled();
        if (ImGui.checkbox("##enabled", enabled)) {
            if (enabled) pipeline.disable(); else pipeline.enable();
        }

        ImGui.sameLine();
        ImGui.text(pipeline.getName().getPath());

        ImGui.sameLine();
        ImGui.setNextItemWidth(200);

        PostProcessStage currentStage = pipeline.getStage();
        if (ImGui.beginCombo("##stage_select", currentStage.name())) {
            for (PostProcessStage stageOption : PostProcessStage.values()) {
                boolean isSelected = (currentStage == stageOption);
                if (ImGui.selectable(stageOption.name(), isSelected)) {
                    movePipelineToStage(pipeline, stageOption);
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }

        ImGui.separator();
        ImGui.popID();
    }

    private void movePipelineToStage(StagedPostProcessPipeline pipeline, PostProcessStage newStage) {
        if (pipeline.getStage() == newStage) return;
        Instances.getPostProcessManager().changePipelineStage(pipeline, newStage);
    }
}