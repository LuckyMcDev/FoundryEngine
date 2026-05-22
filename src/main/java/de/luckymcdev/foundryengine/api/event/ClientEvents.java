package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import org.jetbrains.annotations.ApiStatus;

public class ClientEvents {
    public static final EventGroupHolder<ClientTickEvent.Post> TICK = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_TICK, BlueprintContexts::clientTick);
    public static final EventGroupHolder<ClientStoppedEvent> STOPPED = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_STOPPED, BlueprintContexts::clientStopped);
    public static final EventGroupHolder<ClientStoppingEvent> STOPPING = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_STOPPING, BlueprintContexts::clientStopping);
    public static final EventGroupHolder<ClientChatEvent> CHAT = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CHAT_MESSAGE, BlueprintContexts::clientChat);
    public static final EventGroupHolder<RegisterKeyMappingsEvent> KEY_MAPPINGS = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderGuiEvent.Post> RENDER_GUI = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_RENDER_GUI, BlueprintContexts::renderGui);
    public static final EventGroupHolder<RenderGuiLayerEvent.Post> RENDER_GUI_LAYER = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderHandEvent> RENDER_HAND = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderLevelStageEvent.AfterLevel> RENDER_AFTER_LEVEL = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientPlayerNetworkEvent.LoggingIn> LOGGED_IN = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_LOGGED_IN, BlueprintContexts::clientLoggedIn);
    public static final EventGroupHolder<ClientPlayerNetworkEvent.LoggingOut> LOGGED_OUT = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_LOGGED_OUT, BlueprintContexts::clientLoggedOut);

    public static void tick(EventCallback<ClientTickEvent.Post> cb) {
        TICK.register(cb);
    }

    public static void stopped(EventCallback<ClientStoppedEvent> cb) {
        STOPPED.register(cb);
    }

    public static void stopping(EventCallback<ClientStoppingEvent> cb) {
        STOPPING.register(cb);
    }

    public static void chat(EventCallback<ClientChatEvent> cb) {
        CHAT.register(cb);
    }

    public static void keyMappings(EventCallback<RegisterKeyMappingsEvent> cb) {
        KEY_MAPPINGS.register(cb);
    }

    public static void renderGui(EventCallback<RenderGuiEvent.Post> cb) {
        RENDER_GUI.register(cb);
    }

    public static void renderGuiLayer(EventCallback<RenderGuiLayerEvent.Post> cb) {
        RENDER_GUI_LAYER.register(cb);
    }

    public static void renderHand(EventCallback<RenderHandEvent> cb) {
        RENDER_HAND.register(cb);
    }

    public static void renderAfterLevel(EventCallback<RenderLevelStageEvent.AfterLevel> cb) {
        RENDER_AFTER_LEVEL.register(cb);
    }

    public static void loggedIn(EventCallback<ClientPlayerNetworkEvent.LoggingIn> cb) {
        LOGGED_IN.register(cb);
    }

    public static void loggedOut(EventCallback<ClientPlayerNetworkEvent.LoggingOut> cb) {
        LOGGED_OUT.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(TICK::post);
            bus.addListener(STOPPED::post);
            bus.addListener(STOPPING::post);
            bus.addListener(CHAT::post);
            bus.addListener(RENDER_GUI::post);
            bus.addListener(RENDER_GUI_LAYER::post);
            bus.addListener(RENDER_HAND::post);
            bus.addListener(RENDER_AFTER_LEVEL::post);
            bus.addListener(LOGGED_IN::post);
            bus.addListener(LOGGED_OUT::post);
        }

        public static void clear() {
            TICK.clear();
            STOPPED.clear();
            STOPPING.clear();
            CHAT.clear();
            KEY_MAPPINGS.clear();
            RENDER_GUI.clear();
            RENDER_GUI_LAYER.clear();
            RENDER_HAND.clear();
            RENDER_AFTER_LEVEL.clear();
            LOGGED_IN.clear();
            LOGGED_OUT.clear();
        }
    }
}