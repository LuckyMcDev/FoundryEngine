package de.luckymcdev.foundryengine.client.node;

import java.util.function.Supplier;

public record NodeOption<T>(String name, Supplier<NodeBuilder<T>> factory) {
}