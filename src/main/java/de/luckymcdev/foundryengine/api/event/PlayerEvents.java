package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.ApiStatus;

public class PlayerEvents {
    public static final EventGroupHolder<PlayerEvent.PlayerLoggedInEvent> LOGGED_IN =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_LOGGED_IN, BlueprintContexts::playerLoggedIn);
    public static final EventGroupHolder<PlayerEvent.PlayerLoggedOutEvent> LOGGED_OUT =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_LOGGED_OUT, BlueprintContexts::playerLoggedOut);
    public static final EventGroupHolder<PlayerTickEvent.Post> TICK =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_TICK, BlueprintContexts::playerTick);
    public static final EventGroupHolder<ServerChatEvent> CHAT =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_CHAT, BlueprintContexts::playerChat);
    public static final EventGroupHolder<AdvancementEvent.AdvancementEarnEvent> ADVANCEMENT =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_ADVANCEMENT, BlueprintContexts::playerAdvancement);
    public static final EventGroupHolder<PlayerContainerEvent.Close> CHEST_CLOSED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CHEST_CLOSED, BlueprintContexts::chestClosed);
    public static final EventGroupHolder<PlayerContainerEvent.Open> CHEST_OPENED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_CHEST_OPENED, BlueprintContexts::chestOpened);
    public static final EventGroupHolder<PlayerEvent.PlayerRespawnEvent> RESPAWNED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_PLAYER_RESPAWNED, BlueprintContexts::playerRespawned);
    public static final EventGroupHolder<ClientChatReceivedEvent> DECORATE_CHAT =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_DECORATE_CHAT, BlueprintContexts::decorateChat);

    public static void loggedIn(EventCallback<PlayerEvent.PlayerLoggedInEvent> cb) {
        LOGGED_IN.register(cb);
    }

    public static void loggedOut(EventCallback<PlayerEvent.PlayerLoggedOutEvent> cb) {
        LOGGED_OUT.register(cb);
    }

    public static void tick(EventCallback<PlayerTickEvent.Post> cb) {
        TICK.register(cb);
    }

    public static void chat(EventCallback<ServerChatEvent> cb) {
        CHAT.register(cb);
    }

    public static void advancement(EventCallback<AdvancementEvent.AdvancementEarnEvent> cb) {
        ADVANCEMENT.register(cb);
    }

    public static void chestClosed(EventCallback<PlayerContainerEvent.Close> cb) {
        CHEST_CLOSED.register(cb);
    }

    public static void chestOpened(EventCallback<PlayerContainerEvent.Open> cb) {
        CHEST_OPENED.register(cb);
    }

    public static void respawned(EventCallback<PlayerEvent.PlayerRespawnEvent> cb) {
        RESPAWNED.register(cb);
    }

    public static void decorateChat(EventCallback<ClientChatReceivedEvent> cb) {
        DECORATE_CHAT.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(LOGGED_IN::post);
            bus.addListener(LOGGED_OUT::post);
            bus.addListener(TICK::post);
            bus.addListener(CHAT::post);
            bus.addListener(ADVANCEMENT::post);
            bus.addListener(CHEST_CLOSED::post);
            bus.addListener(CHEST_OPENED::post);
            bus.addListener(RESPAWNED::post);
            bus.addListener(DECORATE_CHAT::post);
        }

        public static void clear() {
            LOGGED_IN.clear();
            LOGGED_OUT.clear();
            TICK.clear();
            CHAT.clear();
            ADVANCEMENT.clear();
            CHEST_CLOSED.clear();
            CHEST_OPENED.clear();
            RESPAWNED.clear();
            DECORATE_CHAT.clear();
        }
    }
}