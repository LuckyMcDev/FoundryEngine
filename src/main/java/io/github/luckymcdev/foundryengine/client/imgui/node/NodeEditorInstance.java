package io.github.luckymcdev.foundryengine.client.imgui.node;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.type.ImInt;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePin;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePinConnectionType;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePinInfo;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePinType;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class NodeEditorInstance<T> {
    public final NodePinType<T> type;
    public final Node root;
    public final Int2ObjectMap<Node> nodes;
    public final Int2ObjectMap<NodePinInfo> pins;
    private final ImInt tempSrc;
    private final ImInt tempDst;
    public float miniMap;
    public int lastId;
    private NodePinInfo lastDroppedPin;

    public NodeEditorInstance(NodePinType<T> type) {
        this(type, type.singleRequiredInput);
    }

    /**
     * Creates an editor with a custom set of pins on the root node.
     *
     * @param type     The primary pin type for this editor.
     * @param rootPins The pins to attach to the root node.
     */
    public NodeEditorInstance(NodePinType<T> type, List<NodePin> rootPins) {
        this.type = type;
        this.root = new Node("Root", rootPins);
        this.nodes = new Int2ObjectLinkedOpenHashMap<>();
        this.pins = new Int2ObjectLinkedOpenHashMap<>();
        this.tempSrc = new ImInt();
        this.tempDst = new ImInt();
        this.miniMap = 0.1F;
        this.lastId = 0;
        addNode(root);
    }

    public int nextId() {
        return ++lastId;
    }

    public void addNode(Node node) {
        if (node.id == 0) {
            node.id = nextId();
        }

        nodes.put(node.id, node);

        for (var pin : node.inputPins) {
            if (pin.id == 0) {
                pin.id = nextId();
            }
            pins.put(pin.id, pin);
        }

        for (var pin : node.outputPins) {
            if (pin.id == 0) {
                pin.id = nextId();
            }
            pins.put(pin.id, pin);
        }
    }

    private void dropNewNode(@Nullable Node node) {
        if (node == null || node.inputPins.isEmpty() && node.outputPins.isEmpty()) {
            return;
        }

        if (lastDroppedPin != null) {
            for (var pin : node.outputPins) {
                if (pin.pin.type() == lastDroppedPin.pin.type()) {
                    pin.inputLink = lastDroppedPin;
                    break;
                }
            }
            lastDroppedPin = null;
        }

        addNode(node);
    }

    public void render(Consumer<Node> onNodeContextMenu) {
        render(onNodeContextMenu, null);
    }

    public void render(Consumer<Node> onNodeContextMenu, Consumer<Node> onNodeBody) {
        boolean mouseRightButton = ImGui.isMouseClicked(1);

        ImNodes.beginNodeEditor();
        boolean nodeEditorHovered = ImNodes.isEditorHovered();
        Node removedNode = null;

        for (var node : nodes.values()) {
            ImNodes.beginNode(node.id);
            ImNodes.beginNodeTitleBar();

            if (node != root) {
                if (ImGui.button("X##delete-node-" + node.id)) {
                    removedNode = node;
                }
                ImGui.sameLine();
            }

            ImGui.text(node.name);
            ImNodes.endNodeTitleBar();
            ImGui.pushItemWidth(130F);

            if (node != root) {
                if (onNodeBody != null) {
                    onNodeBody.accept(node);
                } else {
                    ImGui.textUnformatted("Node Content");
                }
            } else {
                ImGui.textUnformatted("Root Node");
            }

            for (var pin : node.inputPins) {
                ImNodes.beginInputAttribute(pin.id, pin.pin.shape().id);
                ImGui.textUnformatted(pin.pin.label());
                ImNodes.endInputAttribute();
            }

            for (var pin : node.outputPins) {
                ImNodes.beginOutputAttribute(pin.id, pin.pin.shape().id);
                ImGui.textUnformatted(pin.pin.label());
                ImNodes.endOutputAttribute();
            }

            ImGui.popItemWidth();
            ImNodes.endNode();
        }

        for (var pin : pins.values()) {
            if (pin.inputLink != null) {
                ImNodes.link(pin.id, pin.inputLink.id, pin.id);
            }
        }

        if (miniMap > 0F) {
            ImNodes.miniMap(miniMap, ImNodesMiniMapLocation.TopRight);
        }

        ImNodes.endNodeEditor();

        if (ImNodes.isLinkCreated(tempSrc, tempDst)) {
            var src = pins.get(tempSrc.get());
            var dst = pins.get(tempDst.get());
            var in = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? dst : src;
            var out = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? src : dst;
            in.inputLink = out;
        }

        if (ImNodes.isLinkDestroyed(tempSrc)) {
            var pin = pins.get(tempSrc.get());
            if (pin != null) {
                pin.inputLink = null;
            }
        }

        if (ImNodes.isLinkDropped(tempSrc, false)) {
            lastDroppedPin = pins.get(tempSrc.get());
            ImGui.openPopup("###context-menu");
        }

        if (nodeEditorHovered && mouseRightButton && !ImGui.isAnyItemHovered()) {
            ImGui.openPopup("###context-menu");
        }

        if (ImGui.beginPopup("###context-menu")) {
            if (onNodeContextMenu != null) {
                onNodeContextMenu.accept(null);
            }
            ImGui.endPopup();
        }

        if (lastDroppedPin != null && !ImGui.isPopupOpen("###context-menu")) {
            lastDroppedPin = null;
        }

        for (var node : nodes.values()) {
            node.selected = ImNodes.isNodeSelected(node.id);
        }

        for (var pin : pins.values()) {
            pin.inputLinkSelected = pin.inputLink != null && ImNodes.isLinkSelected(pin.id);
        }

        if (ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE)) {
            for (var pin : pins.values()) {
                if (pin.inputLinkSelected) {
                    pin.inputLinkSelected = false;
                    pin.inputLink = null;
                }
            }

            for (var node : nodes.values()) {
                if (node.selected && node != root) {
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

            for (var pin : removedNode.inputPins) {
                pins.remove(pin.id);
            }
            for (var pin : removedNode.outputPins) {
                pins.remove(pin.id);
            }
        }
    }

    public void clear() {
        nodes.clear();
        pins.clear();

        for (var pin : root.inputPins) {
            pin.inputLink = null;
        }

        addNode(root);
    }
}