package de.luckymcdev.foundryengine.common.blueprint.graph;

public record NodePin(NodePinType<?> type, String label, NodePinConnectionType connectionType, NodePinShape shape) {
}