package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.area.AreaEvent;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

public class AreaEvents {

    private static final EventGroupHolder<AreaEvent.AreaEnterEvent> AREA_ENTER =
            new EventGroupHolder<>();

    private static final EventGroupHolder<AreaEvent.AreaLeaveEvent> AREA_LEAVE =
            new EventGroupHolder<>();

    private static final EventGroupHolder<AreaEvent.AreaTickEvent> AREA_TICK =
            new EventGroupHolder<>();

    public static void areaEnter(EventCallback<AreaEvent.AreaEnterEvent> cb) {
        AREA_ENTER.register(cb);
    }

    public static void areaLeave(EventCallback<AreaEvent.AreaLeaveEvent> cb) {
        AREA_LEAVE.register(cb);
    }

    public static void areaTick(EventCallback<AreaEvent.AreaTickEvent> cb) {
        AREA_TICK.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(AREA_ENTER::post);
            bus.addListener(AREA_LEAVE::post);
            bus.addListener(AREA_TICK::post);
        }

        public static void clear() {
            AREA_ENTER.clear();
            AREA_LEAVE.clear();
            AREA_TICK.clear();
        }
    }
}