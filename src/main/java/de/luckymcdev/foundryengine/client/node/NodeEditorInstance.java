package de.luckymcdev.foundryengine.client.node;

import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.ImGuiKey;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

public class NodeEditorInstance<T> {
    public final NodePinType<T> type;
    public final Node<T> root;
    public final Int2ObjectMap<Node<T>> nodes;
    public final Int2ObjectMap<NodePinInfo<T>> pins;
    private final ImInt tempSrc;
    private final ImInt tempDst;
    public float miniMap;
    public int lastId;
    public NodeBuilder<T> rootBuilder;
    private NodePinInfo<T> lastDroppedPin;

    public NodeEditorInstance(NodePinType<T> type) {
        this.type = type;
        this.root = new Node<>(type.singleRequiredInput);
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

    public void addNode(Node<T> node) {
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

    private void removeNode(Node<T> node) {
        if (node == root) return; // don't remove root
        // Remove all pins of this node
        for (var pin : node.inputPins) {
            pins.remove(pin.id);
            // Also clear any links from this input to an output (should be null anyway)
        }
        for (var pin : node.outputPins) {
            pins.remove(pin.id);
            // Break any links from other pins to this output
            for (var other : pins.values()) {
                if (other.inputLink == pin) {
                    other.inputLink = null;
                    other.inputLinkSelected = false;
                }
            }
        }
        nodes.remove(node.id);
    }

    private void dropNewNode(@Nullable Node<T> node) {
        if (node == null || (node.inputPins.isEmpty() && node.outputPins.isEmpty())) {
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

    public boolean content(ImGraphicsExtractor g) {
        boolean update = false;
        boolean mouseRightButton = ImGui.isMouseClicked(1);

        ImNodes.beginNodeEditor();
        boolean nodeEditorHovered = ImNodes.isEditorHovered();
        Node<T> removedNode = null;

        for (var node : nodes.values()) {
            ImNodes.beginNode(node.id);
            ImNodes.beginNodeTitleBar();

            if (node.builder != null) {
                if (ImGui.button(" "+ImIcons.FA.FA_CLOSE+" ")) {
                    removedNode = node;
                }
                ImGui.sameLine();
            }

            String title = node.builder == null ? "Root" : node.builder.getDisplayName();
            ImGui.text(title);
            ImNodes.endNodeTitleBar();
            ImGui.pushItemWidth(130F);

            if (node.builder != null) {
                if (node.builder.render()) {
                    update = true;
                }
            } else {
                // Root node
                ImGui.textUnformatted("Root Node");
                ImGui.textUnformatted("Value:");
                if (rootBuilder != null) {
                    T value = rootBuilder.evaluate();
                    ImGui.textUnformatted(String.valueOf(value));
                } else {
                    g.redTextIf("Invalid", true);
                }
            }

            // Draw pins
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

        // Draw links
        for (var pin : pins.values()) {
            if (pin.inputLink != null) {
                ImNodes.link(pin.id, pin.inputLink.id, pin.id);
            }
        }

        if (miniMap > 0F) {
            ImNodes.miniMap(miniMap, ImNodesMiniMapLocation.TopRight);
        }

        ImNodes.endNodeEditor();

        // Link creation
        if (ImNodes.isLinkCreated(tempSrc, tempDst)) {
            var src = pins.get(tempSrc.get());
            var dst = pins.get(tempDst.get());
            var in = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? dst : src;
            var out = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? src : dst;
            in.inputLink = out;
            update = true;
        }

        // Link dropped
        if (ImNodes.isLinkDropped(tempSrc, false)) {
            lastDroppedPin = pins.get(tempSrc.get());
            ImGui.openPopup("###context-menu");
        }

        if (nodeEditorHovered && mouseRightButton && !ImGui.isAnyItemHovered()) {
            ImGui.openPopup("###context-menu");
        }

        // Context menu
        if (ImGui.beginPopup("###context-menu")) {
            for (var option : type.nodeOptions) {
                if (ImGui.menuItem(option.name())) {
                    NodeBuilder<T> builder = option.factory().get();
                    Node<T> newNode = new Node<>(builder.getPins());
                    newNode.setBuilder(builder);
                    dropNewNode(newNode);
                    update = true;
                }
            }
            ImGui.endPopup();
        }

        if (lastDroppedPin != null && !ImGui.isPopupOpen("###context-menu")) {
            lastDroppedPin = null;
        }

        // Selection tracking
        for (var node : nodes.values()) {
            node.selected = ImNodes.isNodeSelected(node.id);
        }
        for (var pin : pins.values()) {
            pin.inputLinkSelected = pin.inputLink != null && ImNodes.isLinkSelected(pin.id);
        }

        // Delete selected links with DEL
        if (ImGui.isKeyDown(ImGuiKey.Delete)) {
            for (var pin : pins.values()) {
                if (pin.inputLinkSelected) {
                    pin.inputLinkSelected = false;
                    pin.inputLink = null;
                    update = true;
                }
            }
        }

        // Delete node (and clean up)
        if (removedNode != null) {
            update = true;
            // Break all links to this node's outputs (already done in removeNode, but we do it again for safety)
            for (var pin : pins.values()) {
                for (var oPin : removedNode.outputPins) {
                    if (pin.inputLink == oPin) {
                        pin.inputLink = null;
                        pin.inputLinkSelected = false;
                        break;
                    }
                }
            }
            removeNode(removedNode);
        }

        return update;
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