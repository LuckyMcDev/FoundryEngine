package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.neoforge.event.VanillaGameEvent;

public class BundleEvents {
    private static final EventGroup<RegistryEvent> LOAD = new EventGroup<>();
    private static final EventGroup<VanillaGameEvent> VANILLA_GAME = new EventGroup<>();

    public static void registry(EventCallback<RegistryEvent> callback) {
        LOAD.add(callback);
    }

    public static void vanillaGame(EventCallback<VanillaGameEvent> callback) {
        VANILLA_GAME.add(callback);
    }

    public static void _postRegistry(RegistryEvent event) {
        // Fire script-side listeners first, then blueprint equivalents.
        LOAD.post(event);
        Common.getBlueprintManager().executeCommonEvent("Registry");
    }

    public static void _postVanillaGame(VanillaGameEvent event) {
        VANILLA_GAME.post(event);
        Common.getBlueprintManager().executeCommonEvent("Vanilla Game");
    }

    public static void _clear() {
        LOAD.clear();
        VANILLA_GAME.clear();
    }
}