package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

public record InputHandle<T>(String label, NodePinType<T> type) implements PinHandle {
}
