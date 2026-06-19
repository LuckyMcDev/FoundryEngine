package de.luckymcdev.foundryengine.client.editor.panel;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.node.*;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;

import java.util.List;

public class NodeTestPanel extends EditorPanel {
    public static final NodeTestPanel INSTANCE = new NodeTestPanel();

    private final NodeEditorInstance<Double> editor;
    private final NodeBuilder<Double> rootEvalBuilder;

    protected NodeTestPanel() {
        super(new Builder(Common.id("node_test_panel"), "Node Test Panel")
                .icon(ImIcons.FA.FA_BLUETOOTH)
                .category(PanelCategory.EDITOR)
        );

        // Use the predefined DOUBLE type
        NodePinType<Double> doubleType = NodeTypes.DOUBLE;

        editor = new NodeEditorInstance<>(doubleType);

        // Root evaluator – simply evaluates the root's input link
        rootEvalBuilder = new NodeBuilder<>() {
            @Override
            public List<NodePin<Double>> getPins() {
                return List.of();
            }

            @Override
            public boolean render() {
                return false;
            }

            @Override
            public Double evaluate() {
                var rootInput = editor.root.inputPins.get(0);
                if (rootInput.inputLink != null) {
                    return rootInput.inputLink.node.builder.evaluate();
                }
                return 0.0;
            }

            @Override
            public String getDisplayName() {
                return "Root Evaluator";
            }

            @Override
            public void setNode(Node<Double> node) {}
        };

        editor.rootBuilder = rootEvalBuilder;
    }

    @Override
    public void content() {
        beginContent();

        // Render the node editor
        editor.content();

        // Show final result
        ImGui.separator();
        ImGui.text("Final value: " + editor.rootBuilder.evaluate());

        endContent();
    }
}