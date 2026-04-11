package de.luckymcdev.foundryengine.client.imgui.imnodes;

import de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint.BlueprintEngine;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinConnectionType;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinInfo;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinType;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Manages a single node editor canvas.
 */
public class NodeEditorInstance<T> {
    public final NodePinType<T> type;
    public final Int2ObjectMap<Node> nodes;
    public final Int2ObjectMap<NodePinInfo> pins;
    private final ImInt tempSrc = new ImInt();
    private final ImInt tempDst = new ImInt();
    public @Nullable BlueprintEngine engine;
    public float miniMap = 0.1f;
    public int lastId = 0;
    private @Nullable NodePinInfo lastDroppedPin;
    private float spawnX, spawnY;
    private int pendingSpawnId = -1;

    public NodeEditorInstance(NodePinType<T> type, @Nullable BlueprintEngine engine) {
        this.type = type;
        this.engine = engine;
        this.nodes = new Int2ObjectLinkedOpenHashMap<>();
        this.pins = new Int2ObjectLinkedOpenHashMap<>();
    }

    public NodeEditorInstance(NodePinType<T> type) {
        this(type, null);
    }

    public int nextId() {
        return ++lastId;
    }

    public void addNode(Node node) {
        addNode(node, true);
    }

    public void addNode(Node node, boolean positionAtCursor) {
        if (node.id == 0) node.id = nextId();
        nodes.put(node.id, node);

        for (var pin : node.inputPins) {
            if (pin.id == 0) pin.id = nextId();
            pins.put(pin.id, pin);
        }
        for (var pin : node.outputPins) {
            if (pin.id == 0) pin.id = nextId();
            pins.put(pin.id, pin);
        }

        if (positionAtCursor) {
            pendingSpawnId = node.id;
        }
    }

    public Node addNode(BlueprintEngine.NodeTemplate template) {
        if (engine == null) throw new IllegalStateException("Engine reference required for templates");
        Node node = engine.createNode(template);
        addNode(node, true);
        return node;
    }

    public @Nullable NodePinInfo getConnectedInputPin(NodePinInfo outputPin) {
        for (var pin : pins.values()) {
            if (pin.inputLink == outputPin) return pin;
        }
        return null;
    }

    public void clear() {
        nodes.clear();
        pins.clear();
    }

    private boolean canConnect(NodePinInfo a, NodePinInfo b) {
        if (engine != null) return engine.canConnect(a, b);
        return a.pin.type().isCompatibleWith(b.pin.type());
    }

    public void render(Consumer<Node> onNodeContextMenu) {
        render(onNodeContextMenu, null);
    }

    public void render(Consumer<Node> onContextMenu, @Nullable Consumer<Node> onNodeBody) {

        boolean mouseRight = ImGui.isMouseClicked(1);

        ImNodes.beginNodeEditor();
        boolean editorHovered = ImNodes.isEditorHovered();

        //if (editorHovered) {
        float scroll = ImGui.getIO().getMouseWheelH();
        if (scroll != 0f) {
            ImVec2 currentPan = ImNodes.editorContextGetPanning();
            ImGui.getIO().getConfigWindowsMoveFromTitleBarOnly();
            ImVec2 mousePos = ImGui.getMousePos();

            float zoomFactor = 1f + (scroll * 0.1f);
            ImVec2 newPan = new ImVec2(
                    currentPan.x * zoomFactor,
                    currentPan.y * zoomFactor
            );
            ImNodes.editorContextResetPanning(newPan);
        }
        //}

        Node removedNode = null;

        for (var node : nodes.values()) {
            ImNodes.beginNode(node.id);

            ImNodes.beginNodeTitleBar();
            if (ImGui.button("X##del-" + node.id)) removedNode = node;
            ImGui.sameLine();
            ImGui.text(node.name);
            ImNodes.endNodeTitleBar();

            ImGui.pushItemWidth(130f);

            if (onNodeBody != null) onNodeBody.accept(node);

            for (var pin : node.inputPins) {
                pushPinColor(pin.pin.type());
                ImNodes.beginInputAttribute(pin.id, pin.pin.shape().id);
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
                ImNodes.beginOutputAttribute(pin.id, pin.pin.shape().id);
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

        if (ImNodes.isLinkCreated(tempSrc, tempDst)) {
            var src = pins.get(tempSrc.get());
            var dst = pins.get(tempDst.get());
            if (src != null && dst != null) {
                var in = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? dst : src;
                var out = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? src : dst;
                if (canConnect(out, in)) in.inputLink = out;
            }
        }

        if (ImNodes.isLinkDestroyed(tempSrc)) {
            var pin = pins.get(tempSrc.get());
            if (pin != null) pin.inputLink = null;
        }

        if (ImNodes.isLinkDropped(tempSrc, false)) {
            lastDroppedPin = pins.get(tempSrc.get());
            captureSpawnPos();
            ImGui.openPopup("###context-menu");
        }

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

        for (var node : nodes.values()) node.selected = ImNodes.isNodeSelected(node.id);
        for (var pin : pins.values()) pin.inputLinkSelected = pin.inputLink != null && ImNodes.isLinkSelected(pin.id);

        if (ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE)) {
            for (var pin : pins.values()) {
                if (pin.inputLinkSelected) {
                    pin.inputLinkSelected = false;
                    pin.inputLink = null;
                }
            }
            for (var node : nodes.values()) {
                if (node.selected) {
                    removedNode = node;
                    break;
                }
            }
        }

        if (removedNode != null) {
            for (var pin : pins.values()) {
                for (var oPin : removedNode.outputPins) {
                    if (pin.inputLink == oPin) {
                        pin.inputLink = null;
                        pin.inputLinkSelected = false;
                        break;
                    }
                }
            }
            nodes.remove(removedNode.id);
            for (var pin : removedNode.inputPins) pins.remove(pin.id);
            for (var pin : removedNode.outputPins) pins.remove(pin.id);
        }
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
            default -> {
                ImGui.textUnformatted(pin.pin.label() + ": " + v);
            }
        }
    }
}