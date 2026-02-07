package io.github.luckymcdev.client.imgui.node;

import imgui.ImGui;

import java.util.List;

/**
 * Example usage of the node editor system
 */
public class NodeEditorExample {

    public static void main() {
        // Create a node type - declare first, initialize later
        final NodePinType<String>[] stringTypeHolder = new NodePinType[1];

        NodePinType<String> stringType = new NodePinType<>("String", NodePinShape.CIRCLE, nodeConsumer -> {
            // Now we can safely reference stringType through the holder
            NodePinType<String> type = stringTypeHolder[0];

            // Context menu for creating nodes
            if (ImGui.menuItem("Text Node")) {
                Node textNode = new Node("Text", List.of(
                        type.output("Text")
                ));
                nodeConsumer.accept(textNode);
            }

            if (ImGui.menuItem("Combine Node")) {
                Node combineNode = new Node("Combine", List.of(
                        type.required("A"),
                        type.required("B"),
                        type.output("Result")
                ));
                nodeConsumer.accept(combineNode);
            }
        });

        stringTypeHolder[0] = stringType; // Store the reference

        // Create the editor instance
        NodeEditorInstance<String> editor = new NodeEditorInstance<>(stringType);
    }
}