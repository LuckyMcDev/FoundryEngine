package de.luckymcdev.foundryengine.common.graph.model;

import java.util.UUID;

public record LinkModel(
        UUID id,
        UUID fromPin,
        UUID toPin
) {
    public LinkModel {
        if (fromPin.equals(toPin)) {
            throw new IllegalArgumentException("cannot link a pin to itself");
        }
    }
}
