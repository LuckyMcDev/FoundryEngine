package de.luckymcdev.foundryengine.node.client;

public record NodePin<T>(NodePinType<T> type, String label, NodePinConnectionType connectionType, NodePinShape shape) {
	public NodePin<T> withShape(NodePinShape shape) {
		return new NodePin<>(type, label, connectionType, shape);
	}
}