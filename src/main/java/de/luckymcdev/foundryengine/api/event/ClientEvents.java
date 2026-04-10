package de.luckymcdev.foundryengine.api.event;

import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;

public class ClientEvents {
    private static final EventGroup<ClientTickEvent.Post> TICK = new EventGroup<>();
    private static final EventGroup<ClientStoppedEvent> STOPPED = new EventGroup<>();
    private static final EventGroup<ClientStoppingEvent> STOPPING = new EventGroup<>();
    private static final EventGroup<ClientChatEvent> CHAT = new EventGroup<>();
    private static final EventGroup<CustomizeGuiOverlayEvent> CUSTOMIZE_GUI_OVERLAY = new EventGroup<>();
    private static final EventGroup<RegisterKeyMappingsEvent> KEY_MAPPINGS = new EventGroup<>();
    private static final EventGroup<RenderGuiEvent.Post> RENDER_GUI = new EventGroup<>();
    private static final EventGroup<RenderGuiLayerEvent.Post> RENDER_GUI_LAYER = new EventGroup<>();
    private static final EventGroup<RenderHandEvent> RENDER_HAND = new EventGroup<>();
    private static final EventGroup<RenderLevelStageEvent.AfterLevel> RENDER_AFTER_LEVEL = new EventGroup<>();

    public static void tick(EventCallback<ClientTickEvent.Post> callback) {
        TICK.add(callback);
    }

    public static void stopped(EventCallback<ClientStoppedEvent> callback) {
        STOPPED.add(callback);
    }

    public static void stopping(EventCallback<ClientStoppingEvent> callback) {
        STOPPING.add(callback);
    }

    public static void chat(EventCallback<ClientChatEvent> callback) {
        CHAT.add(callback);
    }

    public static void customizeGuiOverlay(EventCallback<CustomizeGuiOverlayEvent> callback) {
        CUSTOMIZE_GUI_OVERLAY.add(callback);
    }

    public static void keyMappings(EventCallback<RegisterKeyMappingsEvent> callback) {
        KEY_MAPPINGS.add(callback);
    }

    public static void renderGui(EventCallback<RenderGuiEvent.Post> callback) {
        RENDER_GUI.add(callback);
    }

    public static void renderGuiLayer(EventCallback<RenderGuiLayerEvent.Post> callback) {
        RENDER_GUI_LAYER.add(callback);
    }

    public static void renderHand(EventCallback<RenderHandEvent> callback) {
        RENDER_HAND.add(callback);
    }

    public static void renderAfterLevel(EventCallback<RenderLevelStageEvent.AfterLevel> callback) {
        RENDER_AFTER_LEVEL.add(callback);
    }

    public static void _postTick(ClientTickEvent.Post event) {
        TICK.post(event);
    }

    public static void _postStopped(ClientStoppedEvent event) {
        STOPPED.post(event);
    }

    public static void _postStopping(ClientStoppingEvent event) {
        STOPPING.post(event);
    }

    public static void _postChat(ClientChatEvent event) {
        CHAT.post(event);
    }

    public static void _postCustomizeGuiOverlay(CustomizeGuiOverlayEvent event) {
        CUSTOMIZE_GUI_OVERLAY.post(event);
    }

    public static void _postKeyMappings(RegisterKeyMappingsEvent event) {
        KEY_MAPPINGS.post(event);
    }

    public static void _postRenderGui(RenderGuiEvent.Post event) {
        RENDER_GUI.post(event);
    }

    public static void _postRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        RENDER_GUI_LAYER.post(event);
    }

    public static void _postRenderHand(RenderHandEvent event) {
        RENDER_HAND.post(event);
    }

    public static void _postRenderAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        RENDER_AFTER_LEVEL.post(event);
    }
}