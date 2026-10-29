package de.luckymcdev.foundryengine.node.client;

public enum NodePinConnectionType {
	OUTPUT("output"),
	REQUIRED_INPUT("required_input"),
	OPTIONAL_INPUT("optional_input");

	public final String id;

	NodePinConnectionType(String id) {
		this.id = id;
	}
}