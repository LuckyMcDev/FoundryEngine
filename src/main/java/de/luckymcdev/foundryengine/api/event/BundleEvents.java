package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.event.data.BundleDataGenEvent;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class BundleEvents {
    public static final EventGroupHolder<RegistryEvent> REGISTRY = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_REGISTRY, BlueprintContexts::bundleRegistry);
    public static final EventGroupHolder<VanillaGameEvent> VANILLA_GAME = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_VANILLA_GAME, BlueprintContexts::vanillaGame);
    public static final EventGroupHolder<FMLCommonSetupEvent> COMMON_SETUP = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_COMMON_SETUP, BlueprintContexts::commonSetup);
    public static final EventGroupHolder<FMLClientSetupEvent> CLIENT_SETUP = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_SETUP, BlueprintContexts::clientSetup);
    public static final EventGroupHolder<FMLDedicatedServerSetupEvent> DEDICATED_SERVER_SETUP = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_DEDICATED_SERVER_SETUP, BlueprintContexts::dedicatedServerSetup);
    public static final EventGroupHolder<InterModProcessEvent> POST_INIT = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_POST_INIT, BlueprintContexts::postInit);
    public static final EventGroupHolder<ServerAboutToStartEvent> SERVER_ABOUT_TO_START = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_ABOUT_TO_START, BlueprintContexts::serverAboutToStart);
    public static final EventGroupHolder<BundleDataGenEvent> DATA_GEN = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_DATA_GEN);
    private static final Map<Class<?>, EventGroupHolder<?>> CUSTOM_EVENTS = new ConcurrentHashMap<>();
    private static @Nullable IEventBus eventBus;

    public static <T extends Event> void custom(Class<T> eventClass, EventCallback<T> callback) {
        custom(eventClass, callback, event -> Map.of());
    }

    public static <T extends Event> void custom(Class<T> eventClass, EventCallback<T> callback, Function<T, Map<String, Object>> contextMapper) {
        @SuppressWarnings("unchecked")
        EventGroupHolder<T> holder = (EventGroupHolder<T>) CUSTOM_EVENTS.computeIfAbsent(eventClass, clazz -> {
            String nodeId = "event.custom_" + clazz.getSimpleName();
            if (eventBus != null) {
                registerCustomOnBus(eventBus, clazz);
            }
            return new EventGroupHolder<>(nodeId, contextMapper);
        });
        holder.register(callback);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerCustomOnBus(IEventBus bus, Class<?> eventClass) {
        bus.addListener((Class) eventClass, (Consumer) event -> {
            EventGroupHolder holder = CUSTOM_EVENTS.get(eventClass);
            if (holder != null) {
                holder.post(event);
            }
        });
    }

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
            eventBus = bus;
            bus.addListener(Internal::postVanillaGame);
            bus.addListener(Internal::postServerAboutToStart);
            bus.addListener(Internal::postDataGen);
            CUSTOM_EVENTS.forEach((eventClass, holder) -> registerCustomOnBus(bus, eventClass));
        }

        @SuppressWarnings("unchecked")
        public static <T extends Event> void postCustom(T event) {
            Class<T> eventClass = (Class<T>) event.getClass();
            EventGroupHolder<T> holder = (EventGroupHolder<T>) CUSTOM_EVENTS.get(eventClass);
            if (holder != null) {
                holder.post(event);
            }
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
            CUSTOM_EVENTS.clear();
        }
    }
}