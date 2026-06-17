package de.luckymcdev.foundryengine.client.node;

import java.util.List;

public interface NodeBuilder {
    List<NodePin> getPins();

    boolean render();

    Object evaluate();

    default String getDisplayName() {
        return "Node";
    }

    default void setNode(Node node) {
    }
}
