package de.luckymcdev.foundryengine.client.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node<T> {
	public final List<NodePinInfo<T>> inputPins;
	public final List<NodePinInfo<T>> outputPins;
	public int id;
	public boolean selected;
	public NodeBuilder<T> builder;

	public Node(List<NodePin<T>> pins) {
		List<NodePinInfo<T>> inputs = new ArrayList<>(1);
		List<NodePinInfo<T>> outputs = new ArrayList<>(1);
		for (var pin : pins) {
			var pinInfo = new NodePinInfo<>(this, pin);
			if (pin.connectionType() == NodePinConnectionType.OUTPUT) {
				outputs.add(pinInfo);
			} else {
				inputs.add(pinInfo);
			}
		}
		// Unmodifiable: nothing should mutate a node's pin list after construction.
		this.inputPins = Collections.unmodifiableList(inputs);
		this.outputPins = Collections.unmodifiableList(outputs);
	}

	/**
	 * Set builder and give it a back-reference.
	 */
	public void setBuilder(NodeBuilder<T> builder) {
		this.builder = builder;
		builder.setNode(this);
	}
}