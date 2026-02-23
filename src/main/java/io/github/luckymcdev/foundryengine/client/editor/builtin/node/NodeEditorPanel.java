package io.github.luckymcdev.foundryengine.client.editor.builtin.node;

import com.mojang.logging.LogUtils;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.imgui.node.*;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The Node Editor Panel provides a visual interface for creating and editing shader nodes.
 * It allows users to create a graph of connected nodes that represent shader operations,
 * and generates GLSL code based on the node connections.
 */
public class NodeEditorPanel extends Panel {
    public static final NodeEditorPanel INSTANCE = new NodeEditorPanel();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NodeEditorInstance<String> nodeEditor;
    private final NodePinType<String> floatType = new NodePinType<>("Float", NodePinShape.CIRCLE, null);
    private final NodePinType<String> vec3Type = new NodePinType<>("Vec3", NodePinShape.CIRCLE, null);

    /**
     * Private constructor to enforce singleton pattern.
     */
    private NodeEditorPanel() {
        super(Common.id("node_editor"), "Node Editor", Shortcut.ctrl(ImGuiKey.N));
        this.nodeEditor = new NodeEditorInstance<>(new NodePinType<>("Float", NodePinShape.CIRCLE, null));
    }

    /**
     * Renders the main content of the node editor panel.
     * Displays the node editor on the left and the shader preview on the right.
     */
    @Override
    public void content() {
        float fullWidth = ImGui.getContentRegionAvailX();
        float leftWidth = Math.max(260F, fullWidth * 0.6F);

        ImGui.beginChild("###node-editor", leftWidth, 0, true);
        nodeEditor.render(node -> {
            if (ImGui.menuItem("UV")) {
                Node uv = new Node("UV", List.of(
                        vec3Type.output("uv")
                ));
                nodeEditor.addNode(uv);
            }

            if (ImGui.menuItem("Color")) {
                Node color = new Node("Color", List.of(
                        floatType.required("R"),
                        floatType.required("G"),
                        floatType.required("B"),
                        vec3Type.output("rgb")
                ));
                nodeEditor.addNode(color);
            }

            if (ImGui.menuItem("Multiply")) {
                Node mul = new Node("Multiply", List.of(
                        floatType.required("A"),
                        floatType.required("B"),
                        floatType.output("Out")
                ));
                nodeEditor.addNode(mul);
            }

            if (ImGui.menuItem("Add")) {
                Node add = new Node("Add", List.of(
                        floatType.required("A"),
                        floatType.required("B"),
                        floatType.output("Out")
                ));
                nodeEditor.addNode(add);
            }

            if (ImGui.menuItem("Sin")) {
                Node sin = new Node("Sin", List.of(
                        floatType.required("X"),
                        floatType.output("Out")
                ));
                nodeEditor.addNode(sin);
            }

            if (ImGui.menuItem("Time")) {
                Node time = new Node("Time", List.of(
                        floatType.output("t")
                ));
                nodeEditor.addNode(time);
            }

            if (ImGui.menuItem("Output")) {
                Node out = new Node("Output", List.of(
                        vec3Type.required("Color")
                ));
                nodeEditor.addNode(out);
            }

            if (ImGui.menuItem("Clear All")) {
                nodeEditor.clear();
            }
        });
        ImGui.endChild();

        ImGui.sameLine();

        ImGui.beginChild("###shader-preview", 0, 0, true);
        renderShaderPreview();
        ImGui.endChild();
    }

    /**
     * Renders the shader preview panel on the right side of the node editor.
     * Displays the generated GLSL code based on the current node connections.
     */
    private void renderShaderPreview() {
        ImGui.textUnformatted("Generated GLSL (Fake)");
        ImGui.separator();

        List<String> lines = new ArrayList<>();
        lines.add("#version 330 core");
        lines.add("in vec2 vUV;");
        lines.add("out vec4 FragColor;");
        lines.add("uniform float uTime;");
        lines.add("");
        lines.add("void main() {");

        List<String> body = new ArrayList<>();
        String finalColor = null;

        for (var node : nodeEditor.nodes.values()) {
            if (node == nodeEditor.root) {
                continue;
            }

            String name = node.name == null ? "Node" : node.name;

            if (name.equalsIgnoreCase("UV")) {
                body.add("  vec2 " + varName(node) + " = vUV;");
                continue;
            }

            if (name.equalsIgnoreCase("Time")) {
                body.add("  float " + varName(node) + " = uTime;");
                continue;
            }

            if (name.equalsIgnoreCase("Sin")) {
                String x = inputValue(node, 0, "0.0");
                body.add("  float " + varName(node) + " = sin(" + x + ");");
                continue;
            }

            if (name.equalsIgnoreCase("Multiply")) {
                String a = inputValue(node, 0, "0.0");
                String b = inputValue(node, 1, "0.0");
                body.add("  float " + varName(node) + " = " + a + " * " + b + ";");
                continue;
            }

            if (name.equalsIgnoreCase("Add")) {
                String a = inputValue(node, 0, "0.0");
                String b = inputValue(node, 1, "0.0");
                body.add("  float " + varName(node) + " = " + a + " + " + b + ";");
                continue;
            }

            if (name.equalsIgnoreCase("Color")) {
                String r = inputValue(node, 0, "0.0");
                String g = inputValue(node, 1, "0.0");
                String b = inputValue(node, 2, "0.0");
                body.add("  vec3 " + varName(node) + " = vec3(" + r + ", " + g + ", " + b + ");");
                continue;
            }

            if (name.equalsIgnoreCase("Output")) {
                finalColor = inputValue(node, 0, "vec3(0.0)");
                continue;
            }

            body.add("  float " + varName(node) + " = 0.0;");
        }

        if (body.isEmpty()) {
            body.add("  // Right click in the editor to add nodes.");
            finalColor = "vec3(0.0)";
        }

        lines.addAll(body);
        if (finalColor == null) {
            finalColor = "vec3(0.0)";
        }
        lines.add("  FragColor = vec4(" + finalColor + ", 1.0);");
        lines.add("}");

        for (String line : lines) {
            ImGui.textUnformatted(line);
        }
    }

    /**
     * Gets the input value for a node's input pin.
     *
     * @param node     The node to get the input value for.
     * @param index    The index of the input pin.
     * @param fallback The fallback value if no input is connected.
     * @return The input value or the fallback value if no input is connected.
     */
    private String inputValue(Node node, int index, String fallback) {
        if (node.inputPins == null || index < 0 || index >= node.inputPins.size()) {
            return fallback;
        }

        NodePinInfo pin = node.inputPins.get(index);
        if (pin == null || pin.inputLink == null) {
            return fallback;
        }

        return varName(pin.inputLink.node);
    }

    /**
     * Generates a variable name for a node based on its name and ID.
     *
     * @param node The node to generate a variable name for.
     * @return The generated variable name.
     */
    private String varName(Node node) {
        String base = node.name == null ? "node" : node.name;
        return sanitizeIdent(base.toLowerCase()) + "_" + node.id;
    }

    /**
     * Sanitizes an identifier to make it a valid GLSL variable name.
     *
     * @param value The identifier to sanitize.
     * @return The sanitized identifier.
     */
    private String sanitizeIdent(String value) {
        if (value == null || value.isBlank()) {
            return "node";
        }

        StringBuilder sb = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            } else if (c == ' ' || c == '-' || c == '.') {
                sb.append('_');
            }
        }

        if (sb.isEmpty() || !Character.isLetter(sb.charAt(0)) && sb.charAt(0) != '_') {
            sb.insert(0, '_');
        }

        return sb.toString();
    }
}
