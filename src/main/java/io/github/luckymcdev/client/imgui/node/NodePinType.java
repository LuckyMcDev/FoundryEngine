package io.github.luckymcdev.client.imgui.node;

import java.util.List;
import java.util.function.Consumer;

public class NodePinType<T> {
	public final String displayName;
	public final NodePinShape defaultShape;
	public final List<NodePin> singleOutput;
	public final List<NodePin> singleRequiredInput;
	public final Consumer<Consumer<Node>> menuBuilder;

	public NodePinType(String displayName, NodePinShape defaultShape, Consumer<Consumer<Node>> menuBuilder) {
		this.displayName = displayName;
		this.defaultShape = defaultShape;
		this.singleOutput = List.of(output("Out"));
		this.singleRequiredInput = List.of(required("In"));
		this.menuBuilder = menuBuilder;
	}

	public NodePinType(String displayName) {
		this(displayName, NodePinShape.FILLED_TRIANGLE, null);
	}

	public NodePin output(String label) {
		return new NodePin(this, label, NodePinConnectionType.OUTPUT, defaultShape);
	}

	public NodePin required(String label) {
		return new NodePin(this, label, NodePinConnectionType.REQUIRED_INPUT, defaultShape);
	}

	public NodePin optional(String label) {
		return new NodePin(this, label, NodePinConnectionType.OPTIONAL_INPUT, defaultShape);
	}
}