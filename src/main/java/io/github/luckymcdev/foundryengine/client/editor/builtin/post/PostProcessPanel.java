package io.github.luckymcdev.foundryengine.client.editor.builtin.post;

import imgui.ImGui;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiTreeNodeFlags;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.util.key.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

/**
 * A panel for managing post-processing pipelines.
 */
public class PostProcessPanel extends Panel {
    public static final PostProcessPanel INSTANCE = new PostProcessPanel();

    private PostProcessPanel() {
        super(Common.id("post_process_panel"), "Post Processing Panel", Shortcut.ctrl(ImGuiKey.P));
    }

    @Override
    public void content() {
        PostProcessManager manager = Client.getPostProcessManager();
        List<PostProcessPipeline> allPipelines = manager.getPipelines();

        ImGui.text("Active Pipelines: " + manager.getEnabledPipelines().size());
        ImGui.sameLine();
        if (ImGui.smallButton("Disable All")) {
            allPipelines.forEach(PostProcessPipeline::disable);
        }

        ImGui.separator();

        Map<PostProcessStage, List<PostProcessPipeline>> byStage = manager.getPipelinesByStage();

        for (PostProcessStage stage : PostProcessStage.values()) {
            List<PostProcessPipeline> stagePipelines = byStage.getOrDefault(stage, List.of());

            if (stagePipelines.isEmpty()) ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.Alpha, 0.5f);

            if (ImGui.collapsingHeader(stage.name() + " (" + stagePipelines.size() + ")", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (stagePipelines.isEmpty()) {
                    ImGui.textDisabled("  No pipelines registered for this stage.");
                } else {
                    for (PostProcessPipeline pipeline : stagePipelines) {
                        renderPipelineRow(pipeline);
                    }
                }
            }

            if (stagePipelines.isEmpty()) ImGui.popStyleVar();
        }
    }

    private void renderPipelineRow(PostProcessPipeline pipeline) {
        ImGui.pushID(pipeline.getName().toString());

        boolean enabled = pipeline.isEnabled();
        if (ImGui.checkbox("##enabled", enabled)) {
            if (enabled) pipeline.disable();
            else pipeline.enable();
        }

        ImGui.sameLine();
        ImGui.text(pipeline.getName().getPath());

        ImGui.sameLine(ImGui.getWindowWidth() - 200);
        ImGui.setNextItemWidth(180);
        if (ImGui.beginCombo("##stage_sel", pipeline.getStage().name())) {
            for (PostProcessStage s : PostProcessStage.values()) {
                if (ImGui.selectable(s.name(), pipeline.getStage() == s)) {
                    pipeline.setStage(s);
                }
            }
            ImGui.endCombo();
        }

        Map<String, PipelineParam<?>> params = pipeline.getParams();
        if (!params.isEmpty()) {
            ImGui.indent();
            if (ImGui.treeNode("Parameters##tree")) {
                for (PipelineParam<?> param : params.values()) {
                    renderParamWidget(pipeline.getName().toString(), param);
                }
                ImGui.treePop();
            }
            ImGui.unindent();
        }

        ImGui.separator();
        ImGui.popID();
    }

    @SuppressWarnings("unchecked")
    private void renderParamWidget(String pipelineId, PipelineParam<?> param) {
        String label = param.getDisplayName() + "##" + pipelineId + "_" + param.getUniformName();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() * 0.6f);

        switch (param.getKind()) {
            case FLOAT -> {
                float[] v = {(Float) param.getValue()};
                if (ImGui.sliderFloat(label, v, param.getMin(), param.getMax())) {
                    ((PipelineParam<Float>) param).setValue(v[0]);
                }
            }
            case INT -> {
                int[] v = {(Integer) param.getValue()};
                if (ImGui.sliderInt(label, v, (int) param.getMin(), (int) param.getMax())) {
                    ((PipelineParam<Integer>) param).setValue(v[0]);
                }
            }
            case BOOLEAN -> {
                boolean v = (Boolean) param.getValue();
                if (ImGui.checkbox(label, v)) {
                    ((PipelineParam<Boolean>) param).setValue(!v);
                }
            }
            case VEC2 -> {
                Vector2f vec = (Vector2f) param.getValue();
                float[] v = {vec.x, vec.y};
                if (ImGui.dragFloat2(label, v)) {
                    ((PipelineParam<Vector2f>) param).setValue(new Vector2f(v[0], v[1]));
                }
            }
            case VEC3 -> {
                Vector3f vec = (Vector3f) param.getValue();
                float[] v = {vec.x, vec.y, vec.z};
                boolean changed = param.isColorPicker() ? ImGui.colorEdit3(label, v) : ImGui.dragFloat3(label, v);
                if (changed) {
                    ((PipelineParam<Vector3f>) param).setValue(new Vector3f(v[0], v[1], v[2]));
                }
            }
        }
    }
}