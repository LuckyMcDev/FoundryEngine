package de.luckymcdev.foundryengine.client.node;

public class NodePinInfo<T> {
	public final Node<T> node;
	public final NodePin<T> pin;
	public int id;
	public NodePinInfo<T> inputLink;
	public boolean inputLinkSelected;

	public NodePinInfo(Node<T> node, NodePin<T> pin) {
		this.node = node;
		this.pin = pin;
		this.id = 0;
		this.inputLink = null;
	}

	@Override
	public String toString() {
		return "#%,d %s [%s, %s]".formatted(id, pin.label(), pin.type().displayName, pin.connectionType().name);
	}
}