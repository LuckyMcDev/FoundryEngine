package io.github.luckymcdev.client.editor.panels;

import imgui.ImGui;
import io.github.luckymcdev.client.editor.Panel;
import io.github.luckymcdev.client.post.PostProcessManager;
import io.github.luckymcdev.client.post.PostProcessPipeline;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;

import java.util.List;

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

        List<PostProcessPipeline> allPipelines = Instances.getPostProcessManager().getPipelines();
        List<PostProcessPipeline> enabledPipelines = Instances.getPostProcessManager().getEnabledPipelines();

        for (PostProcessPipeline pipeline : allPipelines) {
            ImGui.separator();

            String id = pipeline.getProgram().getId().toString();
            boolean isEnabled = enabledPipelines.contains(pipeline);

            ImGui.text("Pipeline: " + id);

            if (!isEnabled) {
                if (ImGui.button("Enable##" + id)) {
                    Instances.getPostProcessManager().enablePipeline(pipeline);
                }
            } else {
                if (ImGui.button("Disable##" + id)) {
                    Instances.getPostProcessManager().disablePipeline(pipeline);
                }
            }
            ImGui.separator();
        }
    }
}
