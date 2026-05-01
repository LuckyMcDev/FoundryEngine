package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import org.jetbrains.annotations.ApiStatus;

public class ClientEvents {
    private static final EventGroup<ClientTickEvent.Post> TICK = new EventGroup<>();
    private static final EventGroup<ClientStoppedEvent> STOPPED = new EventGroup<>();
    private static final EventGroup<ClientStoppingEvent> STOPPING = new EventGroup<>();
    private static final EventGroup<ClientChatEvent> CHAT = new EventGroup<>();
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

    @ApiStatus.Internal
    public static class Internal {
        public static void postTick(ClientTickEvent.Post event) {
            TICK.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_TICK.id);
        }

        public static void postStopped(ClientStoppedEvent event) {
            STOPPED.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_STOPPED.id);
        }

        public static void postStopping(ClientStoppingEvent event) {
            STOPPING.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_CLIENT_STOPPING.id);
        }

        public static void postChat(ClientChatEvent event) {
            CHAT.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_CHAT_MESSAGE.id);
        }

        public static void postKeyMappings(RegisterKeyMappingsEvent event) {
            KEY_MAPPINGS.post(event);
        }

        public static void postRenderGui(RenderGuiEvent.Post event) {
            RENDER_GUI.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_RENDER_GUI.id);
        }

        public static void postRenderGuiLayer(RenderGuiLayerEvent.Post event) {
            RENDER_GUI_LAYER.post(event);
        }

        public static void postRenderHand(RenderHandEvent event) {
            RENDER_HAND.post(event);
        }

        public static void postRenderAfterLevel(RenderLevelStageEvent.AfterLevel event) {
            RENDER_AFTER_LEVEL.post(event);
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
        }
    }
}
