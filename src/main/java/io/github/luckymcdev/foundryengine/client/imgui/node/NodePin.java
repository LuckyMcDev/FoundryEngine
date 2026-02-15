package io.github.luckymcdev.foundryengine.client.imgui.node;

public record NodePin(NodePinType type, String label, NodePinConnectionType connectionType, NodePinShape shape) {
    public NodePin withShape(NodePinShape shape) {
        return new NodePin(type, label, connectionType, shape);
    }
}
