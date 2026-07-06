package de.luckymcdev.foundryengine.client.node.example;

import de.luckymcdev.foundryengine.client.node.Node;
import de.luckymcdev.foundryengine.client.node.NodeBuilder;
import de.luckymcdev.foundryengine.client.node.NodePin;
import de.luckymcdev.foundryengine.client.node.NodePinConnectionType;
import de.luckymcdev.foundryengine.client.node.NodePinShape;
import de.luckymcdev.foundryengine.client.node.NodeTypes;
import imgui.ImGui;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConstantBuilder implements NodeBuilder<Double> {
	private double value = 0.0;
	private Node<Double> node;

	@Override
	public List<NodePin<Double>> getPins() {
		return List.of(
			new NodePin<>(NodeTypes.DOUBLE, "Out", NodePinConnectionType.OUTPUT, NodePinShape.FILLED_CIRCLE)
		);
	}

	@Override
	public boolean render() {
		float[] val = {(float) value};
		boolean changed = ImGui.sliderFloat("Value", val, -10.0f, 10.0f);
		if (changed) {
			value = val[0];
		}
		return changed;
	}

	@Override
	public Double evaluate() {
		return value;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("node.foundryengine.constant");
	}

	@Override
	public void setNode(Node<Double> node) {
		this.node = node;
	}
}