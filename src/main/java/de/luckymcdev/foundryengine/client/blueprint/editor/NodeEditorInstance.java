package de.luckymcdev.foundryengine.client.blueprint.editor;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.*;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Client-only node editor canvas backed by ImGui-Node-Editor.
 */
public class NodeEditorInstance extends BlueprintGraph implements BlueprintContext.EngineAwareGraph {

    private final BlueprintEngine engine;
    private final ImInt tempSrc = new ImInt();
    private final ImInt tempDst = new ImInt();
    public float miniMap = 0.2f;
    private @Nullable NodePinInfo lastDroppedPin;
    private float spawnX, spawnY;
    private int pendingSpawnId = -1;

    public NodeEditorInstance(BlueprintEngine engine) {
        this.engine = engine;
    }

    private static int toImNodesShape(NodePinShape shape) {
        return switch (shape) {
            case CIRCLE -> ImNodesPinShape.Circle;
            case FILLED_CIRCLE -> ImNodesPinShape.CircleFilled;
            case TRIANGLE -> ImNodesPinShape.Triangle;
            case FILLED_TRIANGLE -> ImNodesPinShape.TriangleFilled;
            case SQUARE -> ImNodesPinShape.Quad;
            case FILLED_SQUARE -> ImNodesPinShape.QuadFilled;
        };
    }

    @Override
    public BlueprintEngine getEngine() {
        return engine;
    }

    public void addNode(BlueprintEngine.NodeTemplate template) {
        BlueprintNode node = engine.createNode(template);
        addNode(node, true);
    }

    @Override
    public void addNode(BlueprintNode node, boolean positionAtCursor) {
        super.addNode(node, positionAtCursor);
        if (positionAtCursor) pendingSpawnId = node.id;
    }

    public void render(Consumer<BlueprintNode> onContextMenu) {
        render(onContextMenu, null);
    }

    public void render(Consumer<BlueprintNode> onContextMenu,
                       @Nullable Consumer<BlueprintNode> onNodeBody) {

        boolean mouseRight = ImGui.isMouseClicked(1);

        ImNodes.beginNodeEditor();
        boolean editorHovered = ImNodes.isEditorHovered();

        float scroll = ImGui.getIO().getMouseWheelH();
        if (scroll != 0f) {
            ImVec2 pan = ImNodes.editorContextGetPanning();
            float zoomFactor = 1f + (scroll * 0.1f);
            ImNodes.editorContextResetPanning(new ImVec2(pan.x * zoomFactor, pan.y * zoomFactor));
        }

        BlueprintNode xButtonRemovedNode = null;

        for (var node : nodes.values()) {
            ImNodes.beginNode(node.id);

            ImNodes.beginNodeTitleBar();
            if (ImGui.button("X##del-" + node.id)) xButtonRemovedNode = node;
            ImGui.sameLine();
            ImGui.text(node.name);
            ImNodes.endNodeTitleBar();

            ImGui.pushItemWidth(130f);
            if (onNodeBody != null) onNodeBody.accept(node);

            for (var pin : node.inputPins) {
                pushPinColor(pin.pin.type());
                ImNodes.beginInputAttribute(pin.id, toImNodesShape(pin.pin.shape()));
                popPinColor();

                if (!pin.isConnected() && pin.defaultValue != null) {
                    renderInlineDefault(pin);
                } else {
                    ImGui.textUnformatted(pin.pin.label());
                }
                ImNodes.endInputAttribute();
            }

            for (var pin : node.outputPins) {
                pushPinColor(pin.pin.type());
                ImNodes.beginOutputAttribute(pin.id, toImNodesShape(pin.pin.shape()));
                ImGui.textUnformatted(pin.pin.label());
                ImNodes.endOutputAttribute();
                popPinColor();
            }

            ImGui.popItemWidth();
            ImNodes.endNode();
        }

        for (var pin : pins.values()) {
            if (pin.inputLink != null) {
                pushLinkColor(pin.pin.type());
                ImNodes.link(pin.id, pin.inputLink.id, pin.id);
                popLinkColor();
            }
        }

        if (miniMap > 0f) ImNodes.miniMap(miniMap, ImNodesMiniMapLocation.TopRight);

        ImNodes.endNodeEditor();

        if (pendingSpawnId != -1) {
            ImNodes.setNodeScreenSpacePos(pendingSpawnId, spawnX, spawnY);
            pendingSpawnId = -1;
        }

        handleLinkCreation();
        handleLinkDeletion();
        handleLinkDrop(onContextMenu);
        handleContextMenu(onContextMenu, editorHovered, mouseRight);
        handleNodeAndLinkSelection();
        handleXButtonRemoval(xButtonRemovedNode);
        handleDeleteKey();
    }

    public float[] getNodeGridPos(int nodeId) {
        ImVec2 pos = ImNodes.getNodeGridSpacePos(nodeId);
        return new float[]{pos.x, pos.y};
    }

    public void setNodeGridPos(int nodeId, float x, float y) {
        ImNodes.setNodeGridSpacePos(nodeId, new ImVec2(x, y));
    }

    public void setPanning(float x, float y) {
        ImNodes.editorContextResetPanning(new ImVec2(x, y));
    }

    public float[] getPanning() {
        ImVec2 pan = ImNodes.editorContextGetPanning();
        return new float[]{pan.x, pan.y};
    }

    private void handleLinkCreation() {
        if (ImNodes.isLinkCreated(tempSrc, tempDst)) {
            var src = pins.get(tempSrc.get());
            var dst = pins.get(tempDst.get());
            if (src != null && dst != null) {
                var in = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? dst : src;
                var out = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? src : dst;
                if (engine.canConnect(out, in)) in.inputLink = out;
            }
        }
    }

    private void handleLinkDeletion() {
        if (ImNodes.isLinkDestroyed(tempSrc)) {
            var pin = pins.get(tempSrc.get());
            if (pin != null) pin.inputLink = null;
        }
    }

    private void handleLinkDrop(Consumer<BlueprintNode> onContextMenu) {
        if (ImNodes.isLinkDropped(tempSrc, false)) {
            lastDroppedPin = pins.get(tempSrc.get());
            captureSpawnPos();
            ImGui.openPopup("###context-menu");
        }
    }

    private void handleContextMenu(Consumer<BlueprintNode> onContextMenu,
                                   boolean editorHovered, boolean mouseRight) {
        if (editorHovered && mouseRight && !ImGui.isAnyItemHovered()) {
            captureSpawnPos();
            ImGui.openPopup("###context-menu");
        }

        if (ImGui.beginPopup("###context-menu")) {
            if (onContextMenu != null) onContextMenu.accept(null);
            ImGui.endPopup();
        }

        if (lastDroppedPin != null && !ImGui.isPopupOpen("###context-menu")) {
            lastDroppedPin = null;
        }
    }

    private void handleNodeAndLinkSelection() {
        for (var node : nodes.values()) node.selected = ImNodes.isNodeSelected(node.id);
        for (var pin : pins.values())
            pin.inputLinkSelected = pin.inputLink != null && ImNodes.isLinkSelected(pin.id);
    }

    private void handleXButtonRemoval(@Nullable BlueprintNode node) {
        if (node != null) removeNode(node);
    }

    private void handleDeleteKey() {
        if (!ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE)) return;

        for (var pin : pins.values()) {
            if (pin.inputLinkSelected) {
                pin.inputLinkSelected = false;
                pin.inputLink = null;
            }
        }

        var toRemove = new ArrayList<BlueprintNode>();
        for (var node : nodes.values()) if (node.selected) toRemove.add(node);
        toRemove.forEach(this::removeNode);
    }

    private void captureSpawnPos() {
        spawnX = ImGui.getMousePosX();
        spawnY = ImGui.getMousePosY();
    }

    private void pushPinColor(NodePinType<?> t) {
        ImNodes.pushColorStyle(ImNodesCol.Pin,
                ImGui.colorConvertFloat4ToU32(t.r(), t.g(), t.b(), 1f));
        ImNodes.pushColorStyle(ImNodesCol.PinHovered,
                ImGui.colorConvertFloat4ToU32(
                        Math.min(t.r() + 0.2f, 1f), Math.min(t.g() + 0.2f, 1f),
                        Math.min(t.b() + 0.2f, 1f), 1f));
    }

    private void popPinColor() {
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }

    private void pushLinkColor(NodePinType<?> t) {
        ImNodes.pushColorStyle(ImNodesCol.Link,
                ImGui.colorConvertFloat4ToU32(t.r(), t.g(), t.b(), 1f));
        ImNodes.pushColorStyle(ImNodesCol.LinkHovered,
                ImGui.colorConvertFloat4ToU32(
                        Math.min(t.r() + 0.2f, 1f), Math.min(t.g() + 0.2f, 1f),
                        Math.min(t.b() + 0.2f, 1f), 1f));
        ImNodes.pushColorStyle(ImNodesCol.LinkSelected,
                ImGui.colorConvertFloat4ToU32(1f, 1f, 0f, 1f));
    }

    private void popLinkColor() {
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }

    private void renderInlineDefault(NodePinInfo pin) {
        String id = "##dv_" + pin.id;
        Object v = pin.defaultValue;

        switch (v) {
            case Boolean bool -> {
                var ref = new ImBoolean(bool);
                ImGui.textUnformatted(pin.pin.label() + " ");
                ImGui.sameLine();
                if (ImGui.checkbox(id, ref)) pin.defaultValue = ref.get();
            }
            case Integer i -> {
                var ref = new ImInt(i);
                ImGui.textUnformatted(pin.pin.label());
                if (ImGui.dragInt(id, ref.getData())) pin.defaultValue = ref.get();
            }
            case Float f -> {
                var ref = new ImFloat(f);
                ImGui.textUnformatted(pin.pin.label());
                if (ImGui.dragFloat(id, ref.getData(), 0.1f)) pin.defaultValue = ref.get();
            }
            case String s -> {
                var ref = new ImString(s, 256);
                ImGui.textUnformatted(pin.pin.label());
                if (ImGui.inputText(id, ref)) pin.defaultValue = ref.get();
            }
            default -> ImGui.textUnformatted(pin.pin.label() + ": " + v);
        }
    }
}