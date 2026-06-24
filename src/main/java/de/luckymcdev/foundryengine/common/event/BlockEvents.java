package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.modification.BlockModificationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jetbrains.annotations.ApiStatus;

public class BlockEvents {
    public static final EventGroupHolder<BreakBlockEvent> BROKEN = new EventGroupHolder<>();
    public static final EventGroupHolder<BlockEvent.EntityPlaceEvent> PLACED = new EventGroupHolder<>();
    public static final EventGroupHolder<BlockEvent.NeighborNotifyEvent> NEIGHBOR_NOTIFY = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.LeftClickBlock> LEFT_CLICKED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.RightClickBlock> RIGHT_CLICKED = new EventGroupHolder<>();
    public static final EventGroupHolder<BlockEvent.FarmlandTrampleEvent> FARMLAND_TRAMPLED = new EventGroupHolder<>();
    public static final EventGroupHolder<BlockModificationEvent> BLOCK_MODIFICATION = new EventGroupHolder<>();

    public static void broken(EventCallback<BreakBlockEvent> cb) {
        BROKEN.register(cb);
    }

    public static void placed(EventCallback<BlockEvent.EntityPlaceEvent> cb) {
        PLACED.register(cb);
    }

    public static void neighborNotify(EventCallback<BlockEvent.NeighborNotifyEvent> cb) {
        NEIGHBOR_NOTIFY.register(cb);
    }

    public static void leftClicked(EventCallback<PlayerInteractEvent.LeftClickBlock> cb) {
        LEFT_CLICKED.register(cb);
    }

    public static void rightClicked(EventCallback<PlayerInteractEvent.RightClickBlock> cb) {
        RIGHT_CLICKED.register(cb);
    }

    public static void farmlandTrampled(EventCallback<BlockEvent.FarmlandTrampleEvent> cb) {
        FARMLAND_TRAMPLED.register(cb);
    }

    public static void modification(EventCallback<BlockModificationEvent> callback) {
        BLOCK_MODIFICATION.register(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        static {
            Common.registerEventClear(Internal::clear);
        }

        public static void postBroken(BreakBlockEvent e) {
            BROKEN.post(e);
        }

        public static void postPlaced(BlockEvent.EntityPlaceEvent e) {
            PLACED.post(e);
        }

        public static void postLeftClicked(PlayerInteractEvent.LeftClickBlock e) {
            LEFT_CLICKED.post(e);
        }

        public static void postRightClicked(PlayerInteractEvent.RightClickBlock e) {
            RIGHT_CLICKED.post(e);
        }

        public static void postFarmlandTrampled(BlockEvent.FarmlandTrampleEvent e) {
            FARMLAND_TRAMPLED.post(e);
        }

        public static void postBlockModification(BlockModificationEvent event) {
            BLOCK_MODIFICATION.post(event);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postBroken);
            bus.addListener(Internal::postPlaced);
            bus.addListener(Internal::postLeftClicked);
            bus.addListener(Internal::postRightClicked);
            bus.addListener(Internal::postFarmlandTrampled);
            bus.addListener(Internal::postBlockModification);
        }

        public static void clear() {
            BROKEN.clear();
            PLACED.clear();
            NEIGHBOR_NOTIFY.clear();
            LEFT_CLICKED.clear();
            RIGHT_CLICKED.clear();
            FARMLAND_TRAMPLED.clear();
            BLOCK_MODIFICATION.clear();
        }
    }
}
