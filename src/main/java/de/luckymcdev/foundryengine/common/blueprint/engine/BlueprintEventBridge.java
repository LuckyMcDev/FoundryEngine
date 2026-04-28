package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.api.event.ClientEvents;
import de.luckymcdev.foundryengine.api.event.ServerEvents;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;

public class BlueprintEventBridge {

    public static void subscribe(BlueprintEngine engine, BlueprintGraph graph) {
        ClientEvents.tick(event -> engine.executeEvent("Client Tick", graph));
        ClientEvents.renderGui(event -> engine.executeEvent("Render GUI", graph));
        ClientEvents.chat(event -> engine.executeEvent("Chat Message", graph));

        ServerEvents.started(event -> engine.executeEvent("Server Started", graph));
        ServerEvents.tick(event -> engine.executeEvent("Server Tick", graph));
    }
}