package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

public sealed interface PinHandle permits InputHandle, OutputHandle, ExecInputHandle, ExecOutputHandle {
    String label();

    NodePinType<?> type();
}

