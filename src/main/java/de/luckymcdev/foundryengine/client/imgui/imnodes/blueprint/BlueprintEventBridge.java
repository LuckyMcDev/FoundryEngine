package de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint;

import de.luckymcdev.foundryengine.api.event.ClientEvents;
import de.luckymcdev.foundryengine.api.event.ServerEvents;
import de.luckymcdev.foundryengine.client.imgui.imnodes.NodeEditorInstance;

public class BlueprintEventBridge {

    public static void subscribe(BlueprintEngine engine, NodeEditorInstance<?> editor) {
        ClientEvents.tick(event -> engine.executeEvent("Client Tick", editor));
        ClientEvents.renderGui(event -> engine.executeEvent("Render GUI", editor));
        ClientEvents.chat(event -> engine.executeEvent("Chat Message", editor));

        ServerEvents.started(event -> engine.executeEvent("Server Started", editor));
        ServerEvents.tick(event -> engine.executeEvent("Server Tick", editor));
    }
}