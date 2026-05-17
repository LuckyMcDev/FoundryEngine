package de.luckymcdev.foundryengine.client.render;

import de.luckymcdev.foundryengine.client.Client;

public class EngineDebugRenderer {
    public static void render() {
        Client.getEditorController().renderFeatures();
    }
}
