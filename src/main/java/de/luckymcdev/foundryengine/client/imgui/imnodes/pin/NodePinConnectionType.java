package de.luckymcdev.foundryengine.client.imgui.imnodes.pin;

public enum NodePinConnectionType {
    OUTPUT("output"),
    REQUIRED_INPUT("required_input"),
    OPTIONAL_INPUT("optional_input");

    public final String connectionName;

    NodePinConnectionType(String connectionName) {
        this.connectionName = connectionName;
    }
}