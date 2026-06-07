package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import de.luckymcdev.foundryengine.common.game.stage.GameStageEvent;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

public class StageEvents {
    public static final EventGroupHolder<GameStageEvent.Add> ADDING = new EventGroupHolder<>();
    public static final EventGroupHolder<GameStageEvent.Remove> REMOVING = new EventGroupHolder<>();
    public static final EventGroupHolder<GameStageEvent.Added> ADDED = new EventGroupHolder<>();
    public static final EventGroupHolder<GameStageEvent.Removed> REMOVED = new EventGroupHolder<>();

    public static void adding(EventCallback<GameStageEvent.Add> callback) {
        ADDING.register(callback);
    }

    public static void removing(EventCallback<GameStageEvent.Remove> callback) {
        REMOVING.register(callback);
    }

    public static void added(EventCallback<GameStageEvent.Added> callback) {
        ADDED.register(callback);
    }

    public static void removed(EventCallback<GameStageEvent.Removed> callback) {
        REMOVED.register(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postAdding(GameStageEvent.Add event) {
            ADDING.post(event);
        }

        public static void postRemoving(GameStageEvent.Remove event) {
            REMOVING.post(event);
        }

        public static void postAdded(GameStageEvent.Added event) {
            ADDED.post(event);
        }

        public static void postRemoved(GameStageEvent.Removed event) {
            REMOVED.post(event);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postAdding);
            bus.addListener(Internal::postRemoving);
            bus.addListener(Internal::postAdded);
            bus.addListener(Internal::postRemoved);
        }

        public static void clear() {
            ADDING.clear();
            REMOVING.clear();
            ADDED.clear();
            REMOVED.clear();
        }
    }
}
