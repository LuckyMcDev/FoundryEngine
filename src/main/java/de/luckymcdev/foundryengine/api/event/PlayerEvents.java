package de.luckymcdev.foundryengine.api.event;

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
    public static final EventGroupHolder<PlayerEvent.PlayerLoggedInEvent> LOGGED_IN = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerEvent.PlayerLoggedOutEvent> LOGGED_OUT = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerTickEvent.Post> TICK = new EventGroupHolder<>();
    public static final EventGroupHolder<ServerChatEvent> CHAT = new EventGroupHolder<>();
    public static final EventGroupHolder<AdvancementEvent.AdvancementEarnEvent> ADVANCEMENT = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerContainerEvent.Close> CHEST_CLOSED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerContainerEvent.Open> CHEST_OPENED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerEvent.PlayerRespawnEvent> RESPAWNED = new EventGroupHolder<>();
    public static final EventGroupHolder<ClientChatReceivedEvent> DECORATE_CHAT = new EventGroupHolder<>();

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
        public static void postLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
            LOGGED_IN.post(e);
        }

        public static void postLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
            LOGGED_OUT.post(e);
        }

        public static void postTick(PlayerTickEvent.Post e) {
            TICK.post(e);
        }

        public static void postChat(ServerChatEvent e) {
            CHAT.post(e);
        }

        public static void postAdvancement(AdvancementEvent.AdvancementEarnEvent e) {
            ADVANCEMENT.post(e);
        }

        public static void postChestClosed(PlayerContainerEvent.Close e) {
            CHEST_CLOSED.post(e);
        }

        public static void postChestOpened(PlayerContainerEvent.Open e) {
            CHEST_OPENED.post(e);
        }

        public static void postRespawned(PlayerEvent.PlayerRespawnEvent e) {
            RESPAWNED.post(e);
        }

        public static void postDecorateChat(ClientChatReceivedEvent e) {
            DECORATE_CHAT.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postLoggedIn);
            bus.addListener(Internal::postLoggedOut);
            bus.addListener(Internal::postTick);
            bus.addListener(Internal::postChat);
            bus.addListener(Internal::postAdvancement);
            bus.addListener(Internal::postChestClosed);
            bus.addListener(Internal::postChestOpened);
            bus.addListener(Internal::postRespawned);
            bus.addListener(Internal::postDecorateChat);
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