package io.github.luckymcdev.client.editor.panels;

import imgui.ImGui;
import io.github.luckymcdev.client.editor.Panel;
import io.github.luckymcdev.client.imgui.node.Node;
import io.github.luckymcdev.client.imgui.node.NodeEditorInstance;
import io.github.luckymcdev.client.imgui.node.NodePinShape;
import io.github.luckymcdev.client.imgui.node.NodePinType;
import io.github.luckymcdev.common.Commons;

import java.util.List;

public class NodeEditorPanel extends Panel {
    public static final NodeEditorPanel INSTANCE = new NodeEditorPanel();

    private NodeEditorInstance<String> nodeEditor;
    private NodePinType<String> stringType = new NodePinType<>("String", NodePinShape.CIRCLE, null);;

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
}
