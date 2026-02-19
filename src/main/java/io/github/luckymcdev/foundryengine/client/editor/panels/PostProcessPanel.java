package io.github.luckymcdev.foundryengine.client.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import org.joml.Vector2f;
import org.joml.Vector3f;

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

        if (ImGui.collapsingHeader("Staged Pipelines")) {
            var postProcessManager = Instances.getPostProcessManager();
            Map<PostProcessStage, List<StagedPostProcessPipeline>> pipelinesByStage =
                    postProcessManager.getPipelinesByStage();

            for (PostProcessStage stage : PostProcessStage.values()) {
                List<StagedPostProcessPipeline> stagePipelines =
                        pipelinesByStage.getOrDefault(stage, List.of());

                if (ImGui.treeNode(stage.name() + " (" + stagePipelines.size() + ")")) {
                    if (stagePipelines.isEmpty()) {
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

    // =========================================================================
    // Section renderers
    // =========================================================================

    private void renderNonStagedSection() {
        List<PostProcessPipeline> allPipelines = Instances.getPostProcessManager().getPipelines();
        if (allPipelines.isEmpty()) {
            ImGui.textDisabled("No global pipelines registered");
            return;
        }
        for (PostProcessPipeline pipeline : allPipelines) {
            renderPipelineRow(pipeline, false);
        }
    }

    private void renderStagedPipelineRow(StagedPostProcessPipeline pipeline) {
        ImGui.pushID(pipeline.getName().toString() + "_row");

        // Enable / disable checkbox
        boolean enabled = pipeline.isEnabled();
        if (ImGui.checkbox("##enabled", enabled)) {
            if (enabled) pipeline.disable(); else pipeline.enable();
        }

        ImGui.sameLine();
        ImGui.text(pipeline.getName().getPath());

        // Stage selector
        ImGui.sameLine();
        ImGui.setNextItemWidth(200);
        PostProcessStage currentStage = pipeline.getStage();
        if (ImGui.beginCombo("##stage_select", currentStage.name())) {
            for (PostProcessStage option : PostProcessStage.values()) {
                boolean isSelected = (currentStage == option);
                if (ImGui.selectable(option.name(), isSelected)) {
                    Instances.getPostProcessManager().changePipelineStage(pipeline, option);
                }
                if (isSelected) ImGui.setItemDefaultFocus();
            }
            ImGui.endCombo();
        }

        // Params tree (only shown if the pipeline has declared params)
        renderParamTree(pipeline);

        ImGui.separator();
        ImGui.popID();
    }

    private void renderPipelineRow(PostProcessPipeline pipeline, boolean staged) {
        ImGui.pushID(pipeline.getName().toString());

        boolean enabled = pipeline.isEnabled();
        if (ImGui.checkbox("##enabled", enabled)) {
            if (enabled) pipeline.disable(); else pipeline.enable();
        }
        ImGui.sameLine();
        ImGui.text(pipeline.getName().toString());

        renderParamTree(pipeline);

        ImGui.popID();
    }

    // =========================================================================
    // Param tree
    // =========================================================================

    /**
     * Renders a collapsible tree of ImGui widgets for every {@link PipelineParam}
     * declared by the pipeline. Does nothing if the pipeline has no params.
     */
    private void renderParamTree(PostProcessPipeline pipeline) {
        Map<String, PipelineParam<?>> params = pipeline.getParams();
        if (params.isEmpty()) return;

        ImGui.indent();
        if (ImGui.collapsingHeader("Parameters##" + pipeline.getName(), ImGuiTreeNodeFlags.DefaultOpen)) {
            for (PipelineParam<?> param : params.values()) {
                renderParam(pipeline.getName().toString(), param);
            };
        }
        ImGui.unindent();
    }

    /**
     * Renders the appropriate ImGui widget for a single {@link PipelineParam}.
     * Mutates the param's value in-place; the change is picked up next frame
     * when {@code setupDefaultUniforms} calls {@code param.applyToProgram}.
     */
    @SuppressWarnings("unchecked")
    private void renderParam(String pipelineId, PipelineParam<?> param) {
        String label = param.getDisplayName() + "##" + pipelineId + "_" + param.getUniformName();

        switch (param.getKind()) {
            case FLOAT -> {
                PipelineParam<Float> p = (PipelineParam<Float>) param;
                float[] v = { p.getValue() };
                if (ImGui.sliderFloat(label, v, p.getMin(), p.getMax())) {
                    p.setValue(v[0]);
                }
            }
            case INT -> {
                PipelineParam<Integer> p = (PipelineParam<Integer>) param;
                int[] v = { p.getValue() };
                if (ImGui.sliderInt(label, v, (int) p.getMin(), (int) p.getMax())) {
                    p.setValue(v[0]);
                }
            }
            case BOOLEAN -> {
                PipelineParam<Boolean> p = (PipelineParam<Boolean>) param;
                if (ImGui.checkbox(label, p.getValue())) {
                    p.setValue(p.getValue());
                }
            }
            case VEC2 -> {
                PipelineParam<Vector2f> p = (PipelineParam<Vector2f>) param;
                Vector2f vec = p.getValue();
                float[] v = { vec.x, vec.y };
                if (ImGui.dragFloat2(label, v)) {
                    p.setValue(new Vector2f(v[0], v[1]));
                }
            }
            case VEC3 -> {
                PipelineParam<Vector3f> p = (PipelineParam<Vector3f>) param;
                Vector3f vec = p.getValue();
                float[] v = { vec.x, vec.y, vec.z };
                boolean changed = param.isColorPicker()
                        ? ImGui.colorEdit3(label, v)
                        : ImGui.dragFloat3(label, v);
                if (changed) {
                    p.setValue(new Vector3f(v[0], v[1], v[2]));
                }
            }
        }
    }
}