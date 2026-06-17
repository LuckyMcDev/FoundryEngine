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

    private final NodeEditorInstance editor;
    private final NodeBuilder rootEvalBuilder;

    protected NodeTestPanel() {
        super(Common.id("node_test_panel"), "Node Test Panel", ImIcons.FA.FA_BLUETOOTH, PanelCategory.EDITOR);

        editor = new NodeEditorInstance(NodeTypes.DOUBLE);

        rootEvalBuilder = new NodeBuilder() {
            @Override
            public List<NodePin> getPins() {
                return List.of();
            }

            @Override
            public boolean render() {
                return false;
            }

            @Override
            public Object evaluate() {
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
            public void setNode(Node node) {}
        };

        editor.rootBuilder = rootEvalBuilder;
    }

    @Override
    public void content() {
        beginContent();

        editor.content();

        ImGui.separator();
        ImGui.text("Final value: " + editor.rootBuilder.evaluate());

        endContent();
    }
}
