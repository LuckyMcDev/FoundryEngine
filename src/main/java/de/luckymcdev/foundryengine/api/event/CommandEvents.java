package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.ApiStatus;

public class CommandEvents {
    public static final EventGroupHolder<RegisterCommandsEvent> COMMANDS = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_COMMANDS, BlueprintContexts::registerCommands);
    public static final EventGroupHolder<RegisterClientCommandsEvent> COMMANDS_CLIENT = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_COMMANDS_CLIENT, BlueprintContexts::registerClientCommands);

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

        public static void clear() {
            COMMANDS.clear();
            COMMANDS_CLIENT.clear();
        }
    }
}