package de.luckymcdev.foundryengine.common.graph.model;

import de.luckymcdev.foundryengine.common.graph.type.PinType;

import java.util.UUID;

public record PinModel(
        UUID id,
        PinType type,
        String label,
        PinDirection direction,
        int index
) {
    public PinModel {
        if (index < 0) throw new IllegalArgumentException("index must be >= 0");
    }
}
