package de.luckymcdev.foundryengine.node.client;

import java.util.function.Supplier;

public record NodeOption<T>(String name, Supplier<NodeBuilder<T>> factory) {
}