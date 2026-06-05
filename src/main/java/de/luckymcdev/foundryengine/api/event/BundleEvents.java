package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.event.data.BundleDataGenEvent;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.ApiStatus;

public class BundleEvents {
    public static final EventGroupHolder<RegistryEvent> REGISTRY = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_REGISTRY, BlueprintContexts::bundleRegistry);
    public static final EventGroupHolder<VanillaGameEvent> VANILLA_GAME = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_VANILLA_GAME, BlueprintContexts::vanillaGame);
    public static final EventGroupHolder<FMLCommonSetupEvent> COMMON_SETUP = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_COMMON_SETUP, BlueprintContexts::commonSetup);
    public static final EventGroupHolder<FMLClientSetupEvent> CLIENT_SETUP = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_SETUP, BlueprintContexts::clientSetup);
    public static final EventGroupHolder<FMLDedicatedServerSetupEvent> DEDICATED_SERVER_SETUP = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_DEDICATED_SERVER_SETUP, BlueprintContexts::dedicatedServerSetup);
    public static final EventGroupHolder<InterModProcessEvent> POST_INIT = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_POST_INIT, BlueprintContexts::postInit);
    public static final EventGroupHolder<ServerAboutToStartEvent> SERVER_ABOUT_TO_START = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_ABOUT_TO_START, BlueprintContexts::serverAboutToStart);
    public static final EventGroupHolder<BundleDataGenEvent> DATA_GEN = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_DATA_GEN);

    public static void registry(EventCallback<RegistryEvent> callback) {
        REGISTRY.register(callback);
    }

    public static void vanillaGame(EventCallback<VanillaGameEvent> callback) {
        VANILLA_GAME.register(callback);
    }

    public static void commonSetup(EventCallback<FMLCommonSetupEvent> callback) {
        COMMON_SETUP.register(callback);
    }

    public static void clientSetup(EventCallback<FMLClientSetupEvent> callback) {
        CLIENT_SETUP.register(callback);
    }

    public static void dedicatedServerSetup(EventCallback<FMLDedicatedServerSetupEvent> callback) {
        DEDICATED_SERVER_SETUP.register(callback);
    }

    public static void postInit(EventCallback<InterModProcessEvent> callback) {
        POST_INIT.register(callback);
    }

    public static void serverAboutToStart(EventCallback<ServerAboutToStartEvent> callback) {
        SERVER_ABOUT_TO_START.register(callback);
    }

    public static void dataGen(EventCallback<BundleDataGenEvent> callback) {
        DATA_GEN.register(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postRegistry(RegistryEvent event) {
            REGISTRY.post(event);
        }

        public static void postVanillaGame(VanillaGameEvent event) {
            VANILLA_GAME.post(event);
        }

        public static void postCommonSetup(FMLCommonSetupEvent event) {
            COMMON_SETUP.post(event);
        }

        public static void postClientSetup(FMLClientSetupEvent event) {
            CLIENT_SETUP.post(event);
        }

        public static void postDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
            DEDICATED_SERVER_SETUP.post(event);
        }

        public static void postPostInit(InterModProcessEvent event) {
            POST_INIT.post(event);
        }

        public static void postServerAboutToStart(ServerAboutToStartEvent event) {
            SERVER_ABOUT_TO_START.post(event);
        }

        public static void postDataGen(BundleDataGenEvent event) {
            DATA_GEN.post(event);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postVanillaGame);
            bus.addListener(Internal::postServerAboutToStart);
            bus.addListener(Internal::postDataGen);
        }

        public static void clear() {
            REGISTRY.clear();
            VANILLA_GAME.clear();
            COMMON_SETUP.clear();
            CLIENT_SETUP.clear();
            DEDICATED_SERVER_SETUP.clear();
            POST_INIT.clear();
            DATA_GEN.clear();
            SERVER_ABOUT_TO_START.clear();
        }
    }
}