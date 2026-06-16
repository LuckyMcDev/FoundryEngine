package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import org.jetbrains.annotations.ApiStatus;

public class ClientEvents {
    public static final EventGroupHolder<ClientTickEvent.Post> TICK = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientStoppedEvent> STOPPED = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientStoppingEvent> STOPPING = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientChatEvent> CHAT = new EventGroupHolder<>();
    public static final EventGroupHolder<RegisterKeyMappingsEvent> KEY_MAPPINGS = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderGuiEvent.Post> RENDER_GUI = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderGuiLayerEvent.Post> RENDER_GUI_LAYER = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderHandEvent> RENDER_HAND = new EventGroupHolder<>();
    public static final EventGroupHolder<RenderLevelStageEvent.AfterLevel> RENDER_AFTER_LEVEL = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientPlayerNetworkEvent.LoggingIn> LOGGED_IN = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientPlayerNetworkEvent.LoggingOut> LOGGED_OUT = new EventGroupHolder<>();

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
        public static void postTick(ClientTickEvent.Post e) {
            TICK.post(e);
        }

        public static void postStopped(ClientStoppedEvent e) {
            STOPPED.post(e);
        }

        public static void postStopping(ClientStoppingEvent e) {
            STOPPING.post(e);
        }

        public static void postChat(ClientChatEvent e) {
            CHAT.post(e);
        }

        public static void postRenderGui(RenderGuiEvent.Post e) {
            RENDER_GUI.post(e);
        }

        public static void postRenderGuiLayer(RenderGuiLayerEvent.Post e) {
            RENDER_GUI_LAYER.post(e);
        }

        public static void postRenderHand(RenderHandEvent e) {
            RENDER_HAND.post(e);
        }

        public static void postRenderAfterLevel(RenderLevelStageEvent.AfterLevel e) {
            RENDER_AFTER_LEVEL.post(e);
        }

        public static void postLoggedIn(ClientPlayerNetworkEvent.LoggingIn e) {
            LOGGED_IN.post(e);
        }

        public static void postLoggedOut(ClientPlayerNetworkEvent.LoggingOut e) {
            LOGGED_OUT.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postTick);
            bus.addListener(Internal::postStopped);
            bus.addListener(Internal::postStopping);
            bus.addListener(Internal::postChat);
            bus.addListener(Internal::postRenderGui);
            bus.addListener(Internal::postRenderGuiLayer);
            bus.addListener(Internal::postRenderHand);
            bus.addListener(Internal::postRenderAfterLevel);
            bus.addListener(Internal::postLoggedIn);
            bus.addListener(Internal::postLoggedOut);
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