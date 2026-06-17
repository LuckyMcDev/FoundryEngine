package de.luckymcdev.foundryengine.client.node;

import de.luckymcdev.foundryengine.common.graph.type.PinType;
import de.luckymcdev.foundryengine.client.node.example.AddBuilder;
import de.luckymcdev.foundryengine.client.node.example.ConstantBuilder;
import de.luckymcdev.foundryengine.client.node.example.MultiplyBuilder;

import java.util.List;

public class NodeTypes {
    public static final NodePinType DOUBLE = new NodePinType(
            PinType.DOUBLE,
            List.of(
                    new NodeOption("Constant", ConstantBuilder::new),
                    new NodeOption("Add", AddBuilder::new),
                    new NodeOption("Multiply", MultiplyBuilder::new)
            ),
            ConstantBuilder::new
    );
}
