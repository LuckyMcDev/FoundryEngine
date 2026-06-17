package de.luckymcdev.foundryengine.client.node;

import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class NodeEditorInstance {
    public final NodePinType type;
    public final Node root;
    public final Int2ObjectMap<Node> nodes;
    public final Int2ObjectMap<NodePinInfo> pins;
    private final ImInt tempSrc;
    private final ImInt tempDst;
    public float miniMap;
    public int lastId;
    public NodeBuilder rootBuilder;
    private NodePinInfo lastDroppedPin;

    public NodeEditorInstance(NodePinType type) {
        this.type = type;
        this.root = new Node(type.singleRequiredInput);
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

    private void removeNode(Node node) {
        if (node == root) return;
        for (var pin : node.inputPins) {
            pins.remove(pin.id);
        }
        for (var pin : node.outputPins) {
            pins.remove(pin.id);
            for (var other : pins.values()) {
                if (other.inputLink == pin) {
                    other.inputLink = null;
                    other.inputLinkSelected = false;
                }
            }
        }
        nodes.remove(node.id);
    }

    private void dropNewNode(@Nullable Node node) {
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

    public boolean content() {
        boolean update = false;
        boolean mouseRightButton = ImGui.isMouseClicked(1);

        ImNodes.beginNodeEditor();
        boolean nodeEditorHovered = ImNodes.isEditorHovered();
        Node removedNode = null;

        for (var node : nodes.values()) {
            ImNodes.beginNode(node.id);
            ImNodes.beginNodeTitleBar();

            if (node.builder != null) {
                if (ImGui.button("X")) {
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
                ImGui.textUnformatted("Root Node");
                ImGui.textUnformatted("Value:");
                if (rootBuilder != null) {
                    Object value = rootBuilder.evaluate();
                    ImGui.textUnformatted(String.valueOf(value));
                } else {
                    ImGuiUtils.redTextIf("Invalid", true);
                }
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
            var in_ = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? dst : src;
            var out = src.pin.connectionType() == NodePinConnectionType.OUTPUT ? src : dst;
            if (in_.pin.type().canConnectTo(out.pin.type())) {
                in_.inputLink = out;
                update = true;
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
            for (var option : type.nodeOptions) {
                if (ImGui.menuItem(option.name())) {
                    NodeBuilder builder = option.factory().get();
                    Node newNode = new Node(builder.getPins());
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

        for (var node : nodes.values()) {
            node.selected = ImNodes.isNodeSelected(node.id);
        }
        for (var pin : pins.values()) {
            pin.inputLinkSelected = pin.inputLink != null && ImNodes.isLinkSelected(pin.id);
        }

        if (ImGui.getIO().getKeysDown(GLFW.GLFW_KEY_DELETE)) {
            for (var pin : pins.values()) {
                if (pin.inputLinkSelected) {
                    pin.inputLinkSelected = false;
                    pin.inputLink = null;
                    update = true;
                }
            }
        }

        if (removedNode != null) {
            update = true;
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
