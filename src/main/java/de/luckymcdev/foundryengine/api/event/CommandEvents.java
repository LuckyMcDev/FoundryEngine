package de.luckymcdev.foundryengine.api.event;

import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.ApiStatus;

public class CommandEvents {
    private static final EventGroup<RegisterCommandsEvent> COMMANDS = new EventGroup<>();
    private static final EventGroup<RegisterClientCommandsEvent> COMMANDS_CLIENT = new EventGroup<>();

    public static void register(EventCallback<RegisterCommandsEvent> callback) {
        COMMANDS.add(callback);
    }

    public static void registerClient(EventCallback<RegisterClientCommandsEvent> callback) {
        COMMANDS_CLIENT.add(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void post(RegisterCommandsEvent event) {
            COMMANDS.post(event);
        }

        public static void postClient(RegisterClientCommandsEvent event) {
            COMMANDS_CLIENT.post(event);
        }
    }
}