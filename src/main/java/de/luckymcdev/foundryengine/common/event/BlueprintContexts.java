package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;

public final class BlueprintContexts {
    private static int clientTick = 0;

    public static Map<String, Object> serverTick(ServerTickEvent.Post event) {
        return Map.of(
                "Tick", event.getServer().getTickCount(),
                "DeltaSeconds", 1f / 20f
        );
    }

    public static Map<String, Object> clientTick(ClientTickEvent.Post event) {
        return Map.of(
                "Tick", ++clientTick,
                "DeltaSeconds", 1f / 20f
        );
    }

    public static Map<String, Object> bundleRegistry(RegistryEvent event) {
        return Map.of(BlueprintEngine.CTX_REGISTRY_EVENT, event);
    }

    public static void resetClientTick() {
        clientTick = 0;
    }
}