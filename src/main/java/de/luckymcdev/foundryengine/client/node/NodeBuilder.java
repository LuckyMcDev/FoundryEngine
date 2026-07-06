package de.luckymcdev.foundryengine.client.node;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface NodeBuilder<T> {
	/**
	 * Return the pins this node should have (input and output).
	 */
	List<NodePin<T>> getPins();

	/**
	 * Render custom UI inside the node. Returns true if the value changed.
	 */
	boolean render();

	/**
	 * Compute the node's output value from its inputs.
	 */
	T evaluate();

	/**
	 * Optional display name (used in title bar).
	 */
	default Component getDisplayName() {
		return Component.literal("Node");
	}

	/**
	 * Called after the node is created to give the builder its owning node.
	 */
	default void setNode(Node<T> node) {
	}
}