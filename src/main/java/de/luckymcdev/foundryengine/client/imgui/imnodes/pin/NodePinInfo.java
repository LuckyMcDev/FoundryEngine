package de.luckymcdev.foundryengine.client.imgui.imnodes.pin;

import de.luckymcdev.foundryengine.client.imgui.imnodes.Node;
import org.jetbrains.annotations.Nullable;

public class NodePinInfo {
    public final Node node;
    public final NodePin pin;
    public int id;
    public @Nullable NodePinInfo inputLink;
    public boolean inputLinkSelected;
    public @Nullable Object defaultValue;

    public NodePinInfo(Node node, NodePin pin) {
        this.node = node;
        this.pin = pin;
        this.id = 0;
        this.inputLink = null;
        this.defaultValue = null;
    }

    public boolean isConnected() {
        return inputLink != null;
    }

    @Override
    public String toString() {
        return "#%,d %s [%s, %s]".formatted(id, pin.label(), pin.type().displayName, pin.connectionType().connectionName);
    }
}
