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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Client-only node editor canvas backed by ImGui-Node-Editor.
 */
public class NodeEditorInstance extends BlueprintGraph implements BlueprintContext.EngineAwareGraph {

    private final BlueprintEngine engine;
    private final ImInt tempSrc = new ImInt();
    private final ImInt tempDst = new ImInt();
    private final Map<Integer, ImString> commentTitleBuffers = new HashMap<>();
    private final Map<Integer, ImString> commentBodyBuffers = new HashMap<>();
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

    private static int lighten(int argb, float amount) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.min(255, (int) (r + (255 - r) * amount));
        g = Math.min(255, (int) (g + (255 - g) * amount));
        b = Math.min(255, (int) (b + (255 - b) * amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void render(Consumer<BlueprintNode> onContextMenu) {
        render(onContextMenu, null);
    }

    @Override
    public void removeNode(BlueprintNode node) {
        super.removeNode(node);
        commentTitleBuffers.remove(node.id);
        commentBodyBuffers.remove(node.id);
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

        for (var node : nodes.values()) {
            pushNodeColors(node);
            ImNodes.beginNode(node.id);

            ImNodes.beginNodeTitleBar();
            if (isCommentNode(node)) {
                ImGui.textUnformatted(getCommentTitle(node));
            } else {
                ImGui.textUnformatted(node.name);
            }
            ImNodes.endNodeTitleBar();

            ImGui.pushItemWidth(130f);
            if (isCommentNode(node)) {
                renderCommentBody(node);
            } else if (onNodeBody != null) {
                onNodeBody.accept(node);
            }

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
            popNodeColors();
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
        handleDeleteKey();
    }

    private boolean isCommentNode(BlueprintNode node) {
        return BlueprintEngine.BuiltinNodes.COMMENT.id.equals(node.identifier)
                && BlueprintEngine.Categories.COMMENTS.equals(node.category);
    }

    private String getCommentTitle(BlueprintNode node) {
        Object t = node.data.get("title");
        if (t instanceof String s && !s.isBlank()) return s;
        return "Comment";
    }

    private void renderCommentBody(BlueprintNode node) {
        // Title
        ImString titleBuf = commentTitleBuffers.computeIfAbsent(node.id, id -> {
            String initial = getCommentTitle(node);
            return new ImString(initial, 128);
        });
        if (ImGui.inputText("Title##cmt-title-" + node.id, titleBuf)) {
            node.data.put("title", titleBuf.get());
        }

        // Body
        ImString bodyBuf = commentBodyBuffers.computeIfAbsent(node.id, id -> {
            Object v = node.data.get("text");
            String initial = v instanceof String s ? s : "";
            return new ImString(initial, 2048);
        });

        // A fixed-size multiline field gives the node a "comment box" feel.
        if (ImGui.inputTextMultiline("##cmt-body-" + node.id, bodyBuf, 260f, 140f)) {
            node.data.put("text", bodyBuf.get());
        }
    }

    private void pushNodeColors(BlueprintNode node) {
        int title = engine.getCategoryColor(node.category);
        int titleHovered = lighten(title, 0.10f);
        int titleSelected = lighten(title, 0.18f);

        // Dark node body to match Unreal-esque contrast.
        int bg = 0xFF_242424;
        int bgHovered = 0xFF_2C2C2C;
        int bgSelected = 0xFF_303030;
        int outline = 0xFF_000000;

        ImNodes.pushColorStyle(ImNodesCol.TitleBar, title);
        ImNodes.pushColorStyle(ImNodesCol.TitleBarHovered, titleHovered);
        ImNodes.pushColorStyle(ImNodesCol.TitleBarSelected, titleSelected);
        ImNodes.pushColorStyle(ImNodesCol.NodeBackground, bg);
        ImNodes.pushColorStyle(ImNodesCol.NodeBackgroundHovered, bgHovered);
        ImNodes.pushColorStyle(ImNodesCol.NodeBackgroundSelected, bgSelected);
        ImNodes.pushColorStyle(ImNodesCol.NodeOutline, outline);
    }

    private void popNodeColors() {
        ImNodes.popColorStyle(); // NodeOutline
        ImNodes.popColorStyle(); // NodeBackgroundSelected
        ImNodes.popColorStyle(); // NodeBackgroundHovered
        ImNodes.popColorStyle(); // NodeBackground
        ImNodes.popColorStyle(); // TitleBarSelected
        ImNodes.popColorStyle(); // TitleBarHovered
        ImNodes.popColorStyle(); // TitleBar
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
