package de.luckymcdev.foundryengine.client.blueprint.editor;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.*;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import de.luckymcdev.foundryengine.common.blueprint.serial.BlueprintSerializer;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiKey;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Client-only node editor canvas backed by ImGui-Node-Editor.
 */
public class NodeEditorInstance extends BlueprintGraph implements BlueprintContext.EngineAwareGraph {

    private final BlueprintEngine engine;
    private static final int UNDO_LIMIT = 100;
    private final ImInt tempSrc = new ImInt();
    private final ImInt tempDst = new ImInt();
    private final Map<Integer, ImString> commentTitleBuffers = new HashMap<>();
    private final Map<Integer, ImString> commentBodyBuffers = new HashMap<>();
    private final BlueprintSerializer serializer;
    private final ArrayDeque<String> undoStack = new ArrayDeque<>();
    private final ArrayDeque<String> redoStack = new ArrayDeque<>();
    public float miniMap = 0.2f;
    private @Nullable NodePinInfo lastDroppedPin;
    private float spawnX, spawnY;
    private int pendingSpawnId = -1;

    public NodeEditorInstance(BlueprintEngine engine) {
        this.engine = engine;
        this.serializer = new BlueprintSerializer(engine);
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

    public void addNode(BuiltinNode builtin) {
        pushUndoState();
        BlueprintNode node = builtin.createNode();
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
                if (engine.canConnect(out, in) && in.inputLink != out) {
                    pushUndoState();
                    in.inputLink = out;
                }
            }
        }
    }

    private void handleLinkDeletion() {
        if (ImNodes.isLinkDestroyed(tempSrc)) {
            var pin = pins.get(tempSrc.get());
            if (pin != null && pin.inputLink != null) {
                pushUndoState();
                pin.inputLink = null;
            }
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
        if (!ImGui.isKeyPressed(ImGuiKey.Delete)) return;

        pushUndoState();

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

    private void handleShortcuts(boolean editorHovered) {
        if (!editorHovered) return;
        if (!ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)) return;
        if (ImGui.isAnyItemActive()) return;

        boolean ctrl = ImGui.getIO().getKeyCtrl();
        boolean shift = ImGui.getIO().getKeyShift();

        if (ctrl && !shift && ImGui.isKeyPressed(ImGuiKey.Z)) undo();
        if (ctrl && (ImGui.isKeyPressed(ImGuiKey.Y) || (shift && ImGui.isKeyPressed(ImGuiKey.Z)))) redo();

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.C)) copySelectionToClipboard();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.V)) pasteClipboardAtCursor();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.D)) duplicateSelection();
    }

    private void pushUndoState() {
        // Avoid snapshotting during bulk loads.
        redoStack.clear();
        undoStack.addLast(serializer.serialize(this, this::getNodeGridPos));
        while (undoStack.size() > UNDO_LIMIT) undoStack.removeFirst();
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.addLast(serializer.serialize(this, this::getNodeGridPos));
        applyState(undoStack.removeLast());
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.addLast(serializer.serialize(this, this::getNodeGridPos));
        applyState(redoStack.removeLast());
    }

    private void applyState(String json) {
        commentTitleBuffers.clear();
        commentBodyBuffers.clear();

        var pos = serializer.deserialize(json, this);
        pos.forEach((id, p) -> setNodeGridPos(id, p[0], p[1]));
    }

    private void copySelectionToClipboard() {
        BlueprintGraph clip = new BlueprintGraph();
        Map<Integer, float[]> positions = new HashMap<>();

        Map<Integer, BlueprintNode> nodeClones = new HashMap<>();
        Map<Integer, NodePinInfo> pinClonesById = new HashMap<>();

        for (var node : nodes.values()) {
            if (!node.selected) continue;

            List<NodePin> pins = new ArrayList<>();
            for (var p : node.inputPins)
                pins.add(new NodePin(p.pin.type(), p.pin.label(), p.pin.connectionType(), p.pin.shape()));
            for (var p : node.outputPins)
                pins.add(new NodePin(p.pin.type(), p.pin.label(), p.pin.connectionType(), p.pin.shape()));

            BlueprintNode clone = new BlueprintNode(node.name, node.category, pins);
            clone.id = node.id;
            clone.identifier = node.identifier;
            clone.data.putAll(node.data);
            clone.outputValues.putAll(node.outputValues);

            for (var op : node.inputPins) {
                NodePinInfo cp = clone.inputPin(op.pin.label());
                if (cp != null) {
                    cp.id = op.id;
                    cp.defaultValue = op.defaultValue;
                    pinClonesById.put(cp.id, cp);
                }
            }
            for (var op : node.outputPins) {
                NodePinInfo cp = clone.outputPin(op.pin.label());
                if (cp != null) {
                    cp.id = op.id;
                    pinClonesById.put(cp.id, cp);
                }
            }

            nodeClones.put(node.id, clone);
            clip.addNode(clone, false);
            positions.put(clone.id, getNodeGridPos(node.id));
        }

        if (nodeClones.isEmpty()) return;

        for (var pin : pins.values()) {
            if (pin.inputLink == null) continue;
            if (!pin.node.selected) continue;
            if (!pin.inputLink.node.selected) continue;

            NodePinInfo dst = pinClonesById.get(pin.id);
            NodePinInfo src = pinClonesById.get(pin.inputLink.id);
            if (dst != null && src != null) dst.inputLink = src;
        }

        String json = serializer.serialize(clip, id -> positions.getOrDefault(id, new float[]{0f, 0f}));
        ImGui.setClipboardText(json);
    }

    private void pasteClipboardAtCursor() {
        String json = ImGui.getClipboardText();
        if (json == null || json.isBlank()) return;

        BlueprintGraph temp = new BlueprintGraph();
        Map<Integer, float[]> pos;
        try {
            pos = serializer.deserialize(json, temp);
        } catch (Exception ignored) {
            return;
        }

        if (temp.nodes.isEmpty()) return;

        pushUndoState();

        // Compute a stable offset based on the top-left of the copied selection.
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        for (float[] p : pos.values()) {
            minX = Math.min(minX, p[0]);
            minY = Math.min(minY, p[1]);
        }
        if (minX == Float.POSITIVE_INFINITY) minX = 0f;
        if (minY == Float.POSITIVE_INFINITY) minY = 0f;

        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

        Map<Integer, NodePinInfo> newPinsByOldPinId = new HashMap<>();

        // Create nodes with fresh ids.
        for (var oldNode : temp.nodes.values()) {
            List<NodePin> pins = new ArrayList<>();
            for (var p : oldNode.inputPins)
                pins.add(new NodePin(p.pin.type(), p.pin.label(), p.pin.connectionType(), p.pin.shape()));
            for (var p : oldNode.outputPins)
                pins.add(new NodePin(p.pin.type(), p.pin.label(), p.pin.connectionType(), p.pin.shape()));

            BlueprintNode nn = new BlueprintNode(oldNode.name, oldNode.category, pins);
            nn.identifier = oldNode.identifier;
            nn.data.putAll(oldNode.data);

            super.addNode(nn, false);

            for (var op : oldNode.inputPins) {
                NodePinInfo np = nn.inputPin(op.pin.label());
                if (np != null) {
                    np.defaultValue = op.defaultValue;
                    newPinsByOldPinId.put(op.id, np);
                }
            }
            for (var op : oldNode.outputPins) {
                NodePinInfo np = nn.outputPin(op.pin.label());
                if (np != null) newPinsByOldPinId.put(op.id, np);
            }

            float[] p = pos.getOrDefault(oldNode.id, new float[]{0f, 0f});
            float dx = p[0] - minX;
            float dy = p[1] - minY;
            ImNodes.setNodeScreenSpacePos(nn.id, mouseX + dx, mouseY + dy);
        }

        // Recreate links among pasted nodes.
        for (var oldPin : temp.pins.values()) {
            if (oldPin.inputLink == null) continue;
            NodePinInfo newDst = newPinsByOldPinId.get(oldPin.id);
            NodePinInfo newSrc = newPinsByOldPinId.get(oldPin.inputLink.id);
            if (newDst != null && newSrc != null) newDst.inputLink = newSrc;
        }
    }

    private void duplicateSelection() {
        copySelectionToClipboard();
        pasteClipboardAtCursor();
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
        handleShortcuts(editorHovered);
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
            pushUndoState();
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
            pushUndoState();
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
                if (ImGui.checkbox(id, ref)) {
                    pushUndoState();
                    pin.defaultValue = ref.get();
                }
            }
            case Integer i -> {
                var ref = new ImInt(i);
                ImGui.textUnformatted(pin.pin.label());
                if (ImGui.dragInt(id, ref.getData())) {
                    pushUndoState();
                    pin.defaultValue = ref.get();
                }
            }
            case Float f -> {
                var ref = new ImFloat(f);
                ImGui.textUnformatted(pin.pin.label());
                if (ImGui.dragFloat(id, ref.getData(), 0.1f)) {
                    pushUndoState();
                    pin.defaultValue = ref.get();
                }
            }
            case String s -> {
                var ref = new ImString(s, 256);
                ImGui.textUnformatted(pin.pin.label());
                if (ImGui.inputText(id, ref)) {
                    pushUndoState();
                    pin.defaultValue = ref.get();
                }
            }
            default -> ImGui.textUnformatted(pin.pin.label() + ": " + v);
        }
    }
}
