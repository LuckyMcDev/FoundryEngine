package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

public class NetworkEvents {
    public static final EventGroupHolder<PlayerEvent.PlayerLoggedInEvent> LOGIN = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerEvent.PlayerLoggedOutEvent> LOGOUT = new EventGroupHolder<>();

    public static void login(EventCallback<PlayerEvent.PlayerLoggedInEvent> cb) {
        LOGIN.register(cb);
    }

    public static void logout(EventCallback<PlayerEvent.PlayerLoggedOutEvent> cb) {
        LOGOUT.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postLogin(PlayerEvent.PlayerLoggedInEvent e) {
            LOGIN.post(e);
        }

        public static void postLogout(PlayerEvent.PlayerLoggedOutEvent e) {
            LOGOUT.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postLogin);
            bus.addListener(Internal::postLogout);
        }

        public static void clear() {
            LOGIN.clear();
            LOGOUT.clear();
        }
    }
}