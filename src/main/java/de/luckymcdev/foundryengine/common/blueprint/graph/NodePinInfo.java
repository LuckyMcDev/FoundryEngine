package de.luckymcdev.foundryengine.common.blueprint.graph;

import org.jetbrains.annotations.Nullable;

public class NodePinInfo {
    public final BlueprintNode node;
    public final NodePin pin;
    public int id;
    public @Nullable NodePinInfo inputLink;
    /**
     * Only relevant on the client editor; always {@code false} common/server-side.
     */
    public boolean inputLinkSelected;
    public @Nullable Object defaultValue;

    public NodePinInfo(BlueprintNode node, NodePin pin) {
        this.node = node;
        this.pin = pin;
    }

    public boolean isConnected() {
        return inputLink != null;
    }

    @Override
    public String toString() {
        return "#%,d %s [%s, %s]".formatted(id, pin.label(), pin.type().displayName, pin.connectionType().connectionName);
    }
}