package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

public record ExecInputHandle(String label) implements PinHandle {
    public NodePinType<?> type() {
        return BlueprintTypes.EXEC;
    }
}
