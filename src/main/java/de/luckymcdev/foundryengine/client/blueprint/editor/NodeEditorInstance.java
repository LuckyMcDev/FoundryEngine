package de.luckymcdev.foundryengine.client.blueprint.editor;

import de.luckymcdev.foundryengine.client.editor.panel.tools.CataloguePanel;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.*;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import de.luckymcdev.foundryengine.common.blueprint.serial.BlueprintSerializer;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.extension.imnodes.flag.ImNodesStyleVar;
import imgui.flag.*;
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
    private static final int UNDO_LIMIT = 100;
    private final BlueprintEngine engine;
    private final ImInt tempSrc = new ImInt();
    private final ImInt tempDst = new ImInt();
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

    private static Color lighten(Color color, float amount) {
        float r = Math.min(1, color.r() + (1 - color.r()) * amount);
        float g = Math.min(1, color.g() + (1 - color.g()) * amount);
        float b = Math.min(1, color.b() + (1 - color.b()) * amount);
        return new Color(r, g, b, color.a());
    }

    @Override
    public BlueprintEngine getEngine() {
        return engine;
    }

    public void addNode(BuiltinNode builtin) {
        pushUndoState();
        captureSpawnPos();
        BlueprintNode node = builtin.createNode();
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

    @Override
    public void removeNode(BlueprintNode node) {
        super.removeNode(node);
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

        // Clamp popup to viewport so the search menu never goes off-screen.
        if (ImGui.isPopupOpen("###context-menu")) {
            float mx = Math.min(spawnX, ImGui.getIO().getDisplaySizeX() - 320f);
            float my = Math.min(spawnY, ImGui.getIO().getDisplaySizeY() - 400f);
            ImGui.setNextWindowPos(mx, my, ImGuiCond.Appearing);
        }

        if (ImGui.beginPopup("###context-menu", ImGuiWindowFlags.NoMove)) {
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

        ImNodes.pushStyleVar(ImNodesStyleVar.NodePadding, new ImVec2(8f, 4f));
        ImNodes.pushStyleVar(ImNodesStyleVar.NodeBorderThickness, 1.2f);
        ImNodes.pushStyleVar(ImNodesStyleVar.PinCircleRadius, 5f);
        ImNodes.pushStyleVar(ImNodesStyleVar.PinQuadSideLength, 7f);
        ImNodes.pushStyleVar(ImNodesStyleVar.PinTriangleSideLength, 8f);
        ImNodes.pushStyleVar(ImNodesStyleVar.PinLineThickness, 1.5f);
        ImNodes.pushStyleVar(ImNodesStyleVar.LinkThickness, 2.5f);

        ImNodes.beginNodeEditor();
        boolean editorHovered = ImNodes.isEditorHovered();

        float scroll = ImGui.getIO().getMouseWheelH();
        if (scroll != 0f) {
            ImVec2 pan = ImNodes.editorContextGetPanning();
            float zoomFactor = 1f + (scroll * 0.1f);
            ImNodes.editorContextResetPanning(new ImVec2(pan.x * zoomFactor, pan.y * zoomFactor));
        }

        ImNodes.pushColorStyle(ImNodesCol.GridBackground, 0xFF_1A1A1A);
        ImNodes.pushColorStyle(ImNodesCol.GridLine, 0xFF_2A2A2A);
        ImNodes.pushColorStyle(ImNodesCol.GridLinePrimary, 0xFF_333333);

        for (var node : nodes.values()) {
            pushNodeColors(node);
            ImNodes.beginNode(node.id);

            ImNodes.beginNodeTitleBar();
            ImGui.pushStyleColor(ImGuiCol.Text, 0xFF_FFFFFF);
            ImGui.textUnformatted(node.name);
            ImGui.popStyleColor();
            ImNodes.endNodeTitleBar();

            ImGui.pushItemWidth(140f);
            if (onNodeBody != null) {
                onNodeBody.accept(node);
            }

            for (var pin : node.inputPins) {
                pushPinColor(pin.pin.type());
                ImNodes.beginInputAttribute(pin.id, toImNodesShape(pin.pin.shape()));
                popPinColor();

                ImGui.pushStyleColor(ImGuiCol.Text, 0xFF_BBBBBB);
                if (!pin.isConnected() && pin.defaultValue != null) {
                    renderInlineDefault(pin);
                } else {
                    ImGui.textUnformatted(pin.pin.label());
                }
                ImGui.popStyleColor();
                ImNodes.endInputAttribute();
            }

            for (var pin : node.outputPins) {
                pushPinColor(pin.pin.type());
                ImNodes.beginOutputAttribute(pin.id, toImNodesShape(pin.pin.shape()));
                ImGui.pushStyleColor(ImGuiCol.Text, 0xFF_BBBBBB);
                ImGui.textUnformatted(pin.pin.label());
                ImGui.popStyleColor();
                ImNodes.endOutputAttribute();
                popPinColor();
            }

            ImGui.popItemWidth();
            ImNodes.endNode();
            popNodeColors();
        }

        ImNodes.popColorStyle(); // GridLinePrimary
        ImNodes.popColorStyle(); // GridLine
        ImNodes.popColorStyle(); // GridBackground

        // ── Links ─────────────────────────────────────────────────────
        for (var pin : pins.values()) {
            if (pin.inputLink != null) {
                pushLinkColor(pin.pin.type());
                ImNodes.link(pin.id, pin.inputLink.id, pin.id);
                popLinkColor();
            }
        }

        if (miniMap > 0f) ImNodes.miniMap(miniMap, ImNodesMiniMapLocation.TopRight);

        ImNodes.endNodeEditor();

        ImNodes.popStyleVar(); // LinkThickness
        ImNodes.popStyleVar(); // PinLineThickness
        ImNodes.popStyleVar(); // PinTriangleSideLength
        ImNodes.popStyleVar(); // PinQuadSideLength
        ImNodes.popStyleVar(); // PinCircleRadius
        ImNodes.popStyleVar(); // NodeBorderThickness
        ImNodes.popStyleVar(); // NodePadding

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

    private void pushNodeColors(BlueprintNode node) {
        Color title = engine.getCategoryColor(node.category);
        Color titleHovered = lighten(title, 0.12f);
        Color titleSelected = lighten(title, 0.20f);

        Color titleBar = new Color(title.r(), title.g(), title.b(), 0xF0 / 255f);

        float tr = title.r();
        float tg = title.g();
        float tb = title.b();
        int bgTint = ((int) (tr * 16f) << 16) | ((int) (tg * 16f) << 8) | (int) (tb * 16f);
        int bg = 0xFF_1E1E1E | bgTint;
        int bgHover = 0xFF_262626 | bgTint;
        int bgSel = 0xFF_2C2C2C | (bgTint * 2 & 0xFF_FFFFFF);
        ImNodes.pushColorStyle(ImNodesCol.TitleBar, titleBar.argb());
        ImNodes.pushColorStyle(ImNodesCol.TitleBarHovered, lighten(titleBar, 0.12f).argb());
        ImNodes.pushColorStyle(ImNodesCol.TitleBarSelected, lighten(titleBar, 0.20f).argb());
        ImNodes.pushColorStyle(ImNodesCol.NodeBackground, bg);
        ImNodes.pushColorStyle(ImNodesCol.NodeBackgroundHovered, bgHover);
        ImNodes.pushColorStyle(ImNodesCol.NodeBackgroundSelected, bgSel);
    }

    private void popNodeColors() {
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
        // If the pin's type has a dedicated renderer, use it.
        var renderer = pin.pin.type().renderer;
        if (renderer != null) {
            renderer.render(pin, this::pushUndoState);
            acceptCatalogueDrop(pin);
            return;
        }

        // Fallback: render by Java type
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

        // Catalogue drag-drop target for registry-based pins
        if (!pin.isConnected()) {
            acceptCatalogueDrop(pin);
        }
    }

    private void acceptCatalogueDrop(NodePinInfo pin) {
        String typeName = pin.pin.type().displayName;
        boolean compatible = switch (typeName) {
            case "ItemStack", "BlockState", "EntityType", "Effect", "Enchantment",
                 "Particle", "SoundEvent", "Recipe" -> true;
            default -> false;
        };
        if (!compatible) return;

        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("CATALOGUE_ENTRY");
            if (payload instanceof CataloguePanel.CataloguePayload data) {
                pushUndoState();
                pin.defaultValue = data.id().toString();
            }
            ImGui.endDragDropTarget();
        }
    }
}