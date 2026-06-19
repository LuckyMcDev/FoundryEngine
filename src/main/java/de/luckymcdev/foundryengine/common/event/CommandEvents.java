package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.ApiStatus;

public class CommandEvents {
    public static final EventGroupHolder<RegisterCommandsEvent> COMMANDS = new EventGroupHolder<>();
    public static final EventGroupHolder<RegisterClientCommandsEvent> COMMANDS_CLIENT = new EventGroupHolder<>();

    public static void register(EventCallback<RegisterCommandsEvent> callback) {
        COMMANDS.register(callback);
    }

    public static void registerClient(EventCallback<RegisterClientCommandsEvent> callback) {
        COMMANDS_CLIENT.register(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void post(RegisterCommandsEvent event) {
            COMMANDS.post(event);
        }

        public static void postClient(RegisterClientCommandsEvent event) {
            COMMANDS_CLIENT.post(event);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::post);
            bus.addListener(Internal::postClient);
        }

        public static void clear() {
            COMMANDS.clear();
            COMMANDS_CLIENT.clear();
        }

        static {
            Common.registerEventClear(Internal::clear);
        }
    }
}
