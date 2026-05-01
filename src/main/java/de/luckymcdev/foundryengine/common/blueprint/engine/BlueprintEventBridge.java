package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.api.event.ClientEvents;
import de.luckymcdev.foundryengine.api.event.ServerEvents;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;

public class BlueprintEventBridge {

    public static void subscribe(BlueprintEngine engine, BlueprintGraph graph) {
        ClientEvents.tick(event -> engine.executeEvent(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_TICK.id, graph));
        ClientEvents.renderGui(event -> engine.executeEvent(BlueprintEngine.BuiltinNodes.EVENT_RENDER_GUI.id, graph));
        ClientEvents.chat(event -> engine.executeEvent(BlueprintEngine.BuiltinNodes.EVENT_CHAT_MESSAGE.id, graph));

        ServerEvents.started(event -> engine.executeEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTED.id, graph));
        ServerEvents.tick(event -> engine.executeEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TICK.id, graph));
    }
}
