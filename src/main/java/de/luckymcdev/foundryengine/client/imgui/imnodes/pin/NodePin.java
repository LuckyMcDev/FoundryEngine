package de.luckymcdev.foundryengine.client.imgui.imnodes.pin;

public record NodePin(NodePinType<?> type, String label, NodePinConnectionType connectionType, NodePinShape shape) {
}
