package io.github.luckymcdev.foundryengine.client.editor.builtin;

import com.mojang.logging.LogUtils;
import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.imgui.node.Node;
import io.github.luckymcdev.foundryengine.client.imgui.node.NodeEditorInstance;
import io.github.luckymcdev.foundryengine.client.imgui.node.NodePinShape;
import io.github.luckymcdev.foundryengine.client.imgui.node.NodePinType;
import io.github.luckymcdev.foundryengine.common.Commons;
import org.slf4j.Logger;

import java.util.List;

/**
 * The Node Editor Panel
 */
public class NodeEditorPanel extends Panel {
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * The constant INSTANCE.
     */
    public static final NodeEditorPanel INSTANCE = new NodeEditorPanel();

    private final NodeEditorInstance<String> nodeEditor;
    private final NodePinType<String> stringType = new NodePinType<>("String", NodePinShape.CIRCLE, null);

    private NodeEditorPanel() {
        super(Commons.id("node_editor"), "Node Editor");
        this.nodeEditor = new NodeEditorInstance<>(new NodePinType<>("String", NodePinShape.CIRCLE, null));
    }

    @Override
    public void content() {
        nodeEditor.render(node -> {
            // Context menu for creating nodes
            if (ImGui.menuItem("Text Node")) {
                Node textNode = new Node("Text", List.of(
                        stringType.output("Text")
                ));
                nodeEditor.addNode(textNode);
            }

            if (ImGui.menuItem("Combine Node")) {
                Node combineNode = new Node("Combine", List.of(
                        stringType.required("A"),
                        stringType.required("B"),
                        stringType.output("Result")
                ));
                nodeEditor.addNode(combineNode);
            }

            if (ImGui.menuItem("Clear All")) {
                nodeEditor.clear();
            }
        });

    }

    @Override
    public void tick() {
        LOGGER.info("Ticked Panel Node editor.");
    }
}
