package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public class ServerEvents {
    private static final EventGroup<ServerAboutToStartEvent> ABOUT_TO_START = new EventGroup<>();
    private static final EventGroup<ServerStartedEvent> STARTED = new EventGroup<>();
    private static final EventGroup<ServerStartingEvent> STARTING = new EventGroup<>();
    private static final EventGroup<ServerStoppedEvent> STOPPED = new EventGroup<>();
    private static final EventGroup<ServerStoppingEvent> STOPPING = new EventGroup<>();
    private static final EventGroup<ServerTickEvent.Post> TICK = new EventGroup<>();

    public static void aboutToStart(EventCallback<ServerAboutToStartEvent> callback) {
        ABOUT_TO_START.add(callback);
    }

    public static void started(EventCallback<ServerStartedEvent> callback) {
        STARTED.add(callback);
    }

    public static void starting(EventCallback<ServerStartingEvent> callback) {
        STARTING.add(callback);
    }

    public static void stopped(EventCallback<ServerStoppedEvent> callback) {
        STOPPED.add(callback);
    }

    public static void stopping(EventCallback<ServerStoppingEvent> callback) {
        STOPPING.add(callback);
    }

    public static void tick(EventCallback<ServerTickEvent.Post> callback) {
        TICK.add(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postAboutToStart(ServerAboutToStartEvent event) {
            ABOUT_TO_START.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_ABOUT_TO_START.id);
        }

        public static void postStarted(ServerStartedEvent event) {
            STARTED.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTED.id);
        }

        public static void postStarting(ServerStartingEvent event) {
            STARTING.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTING.id);
        }

        public static void postStopped(ServerStoppedEvent event) {
            STOPPED.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPED.id);
        }

        public static void postStopping(ServerStoppingEvent event) {
            STOPPING.post(event);
            Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPING.id);
        }

        public static void postTick(ServerTickEvent.Post event) {
            TICK.post(event);
            Common.getBlueprintManager().executeCommonEvent(
                    BlueprintEngine.BuiltinNodes.EVENT_SERVER_TICK.id,
                    Map.of(
                            "Tick", event.getServer().getTickCount(),
                            "DeltaSeconds", 1f / 20f
                    )
            );
        }

        public static void clear() {
            ABOUT_TO_START.clear();
            STARTED.clear();
            STARTING.clear();
            STOPPED.clear();
            STOPPING.clear();
            TICK.clear();
        }
    }
}
