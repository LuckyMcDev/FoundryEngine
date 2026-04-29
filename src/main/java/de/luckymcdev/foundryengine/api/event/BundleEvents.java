package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.neoforge.event.VanillaGameEvent;
import org.jetbrains.annotations.ApiStatus;

public class BundleEvents {
    private static final EventGroup<RegistryEvent> LOAD = new EventGroup<>();
    private static final EventGroup<VanillaGameEvent> VANILLA_GAME = new EventGroup<>();

    public static void registry(EventCallback<RegistryEvent> callback) {
        LOAD.add(callback);
    }

    public static void vanillaGame(EventCallback<VanillaGameEvent> callback) {
        VANILLA_GAME.add(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postRegistry(RegistryEvent event) {
            LOAD.post(event);
            Common.getBlueprintManager().executeCommonEvent("Registry");
        }

        public static void postVanillaGame(VanillaGameEvent event) {
            VANILLA_GAME.post(event);
            Common.getBlueprintManager().executeCommonEvent("Vanilla Game");
        }

        public static void clear() {
            LOAD.clear();
            VANILLA_GAME.clear();
        }
    }
}