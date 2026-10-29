package de.luckymcdev.foundryengine.node.client;

import de.luckymcdev.foundryengine.node.client.example.AddBuilder;
import de.luckymcdev.foundryengine.node.client.example.ConstantBuilder;
import de.luckymcdev.foundryengine.node.client.example.MultiplyBuilder;

import java.util.List;

public class NodeTypes {
	public static final NodePinType<Double> DOUBLE = new NodePinType<>(
		"Number",
		NodePinShape.FILLED_TRIANGLE,
		List.of(
			new NodeOption<>("Constant", ConstantBuilder::new),
			new NodeOption<>("Add", AddBuilder::new),
			new NodeOption<>("Multiply", MultiplyBuilder::new)
		),
		ConstantBuilder::new // default builder
	);
}