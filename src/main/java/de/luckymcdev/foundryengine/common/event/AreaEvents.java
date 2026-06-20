package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.ApiStatus;

public class AreaEvents {
    public static final EventGroupHolder<ServerLevel> ON_LOAD = new EventGroupHolder<>();

    public static void register(EventCallback<ServerLevel> cb) {
        ON_LOAD.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postLoad(LevelEvent.Load event) {
            if (event.getLevel() instanceof ServerLevel level) {
                Common.getAreaManager().onLevelLoad(event);
                ON_LOAD.post(level);
            }
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postLoad);
            bus.addListener(Common.getAreaManager()::onLevelTick);
            bus.addListener(Common.getAreaManager()::onServerStopping);
            bus.addListener(Common.getAreaManager()::onBlockBreak);
            bus.addListener(Common.getAreaManager()::onBlockPlace);
        }

        public static void clear() {
            ON_LOAD.clear();
        }

        static {
            Common.registerEventClear(Internal::clear);
        }
    }
}
