package de.luckymcdev.foundryengine.client.node;

import java.util.function.Supplier;

public record NodeOption(String name, Supplier<NodeBuilder> factory) {
}
