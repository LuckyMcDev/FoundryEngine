package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jetbrains.annotations.ApiStatus;

public class BlockEvents {
    public static final EventGroupHolder<BreakBlockEvent> BROKEN = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_BROKEN, BlueprintContexts::blockBroken);
    public static final EventGroupHolder<BlockEvent.EntityPlaceEvent> PLACED = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_PLACED, BlueprintContexts::blockPlaced);
    public static final EventGroupHolder<BlockEvent.NeighborNotifyEvent> NEIGHBOR_NOTIFY = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.LeftClickBlock> LEFT_CLICKED = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_LEFT_CLICKED, BlueprintContexts::blockLeftClicked);
    public static final EventGroupHolder<PlayerInteractEvent.RightClickBlock> RIGHT_CLICKED = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_BLOCK_RIGHT_CLICKED, BlueprintContexts::blockRightClicked);
    public static final EventGroupHolder<BlockEvent.FarmlandTrampleEvent> FARMLAND_TRAMPLED = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_FARMLAND_TRAMPLED, BlueprintContexts::blockFarmlandTrampled);

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

    @ApiStatus.Internal
    public static class Internal {
        public static void postBroken(BreakBlockEvent e) {
            BROKEN.post(e);
        }

        public static void postPlaced(BlockEvent.EntityPlaceEvent e) {
            PLACED.post(e);
        }

        public static void postNeighborNotify(BlockEvent.NeighborNotifyEvent e) {
            NEIGHBOR_NOTIFY.post(e);
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

        public static void clear() {
            BROKEN.clear();
            PLACED.clear();
            NEIGHBOR_NOTIFY.clear();
            LEFT_CLICKED.clear();
            RIGHT_CLICKED.clear();
            FARMLAND_TRAMPLED.clear();
        }
    }
}