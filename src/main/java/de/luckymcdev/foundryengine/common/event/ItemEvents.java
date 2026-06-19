package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.modification.ItemModificationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.*;
import org.jetbrains.annotations.ApiStatus;

public class ItemEvents {
    public static final EventGroupHolder<ItemEntityPickupEvent.Post> PICKED_UP = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerDestroyItemEvent> DESTROYED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.RightClickItem> RIGHT_CLICKED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerEvent.ItemCraftedEvent> CRAFTED = new EventGroupHolder<>();
    public static final EventGroupHolder<ItemTossEvent> DROPPED = new EventGroupHolder<>();
    public static final EventGroupHolder<LivingEntityUseItemEvent.Finish> FOOD_EATEN = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerEvent.ItemSmeltedEvent> SMELTED = new EventGroupHolder<>();
    public static final EventGroupHolder<ItemTooltipEvent> DYNAMIC_TOOLTIPS = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.EntityInteract> ENTITY_INTERACTED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.LeftClickEmpty> FIRST_LEFT_CLICKED = new EventGroupHolder<>();
    public static final EventGroupHolder<PlayerInteractEvent.RightClickEmpty> FIRST_RIGHT_CLICKED = new EventGroupHolder<>();
    public static final EventGroupHolder<ItemModificationEvent> ITEM_MODIFICATION = new EventGroupHolder<>();

    public static void pickedUp(EventCallback<ItemEntityPickupEvent.Post> cb) {
        PICKED_UP.register(cb);
    }

    public static void destroyed(EventCallback<PlayerDestroyItemEvent> cb) {
        DESTROYED.register(cb);
    }

    public static void rightClicked(EventCallback<PlayerInteractEvent.RightClickItem> cb) {
        RIGHT_CLICKED.register(cb);
    }

    public static void crafted(EventCallback<PlayerEvent.ItemCraftedEvent> cb) {
        CRAFTED.register(cb);
    }

    public static void dropped(EventCallback<ItemTossEvent> cb) {
        DROPPED.register(cb);
    }

    public static void foodEaten(EventCallback<LivingEntityUseItemEvent.Finish> cb) {
        FOOD_EATEN.register(cb);
    }

    public static void smelted(EventCallback<PlayerEvent.ItemSmeltedEvent> cb) {
        SMELTED.register(cb);
    }

    public static void dynamicTooltips(EventCallback<ItemTooltipEvent> cb) {
        DYNAMIC_TOOLTIPS.register(cb);
    }

    public static void entityInteracted(EventCallback<PlayerInteractEvent.EntityInteract> cb) {
        ENTITY_INTERACTED.register(cb);
    }

    public static void firstLeftClicked(EventCallback<PlayerInteractEvent.LeftClickEmpty> cb) {
        FIRST_LEFT_CLICKED.register(cb);
    }

    public static void firstRightClicked(EventCallback<PlayerInteractEvent.RightClickEmpty> cb) {
        FIRST_RIGHT_CLICKED.register(cb);
    }

    public static void modification(EventCallback<ItemModificationEvent> callback) {
        ITEM_MODIFICATION.register(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postPickedUp(ItemEntityPickupEvent.Post e) {
            PICKED_UP.post(e);
        }

        public static void postDestroyed(PlayerDestroyItemEvent e) {
            DESTROYED.post(e);
        }

        public static void postRightClicked(PlayerInteractEvent.RightClickItem e) {
            RIGHT_CLICKED.post(e);
        }

        public static void postCrafted(PlayerEvent.ItemCraftedEvent e) {
            CRAFTED.post(e);
        }

        public static void postDropped(ItemTossEvent e) {
            DROPPED.post(e);
        }

        public static void postFoodEaten(LivingEntityUseItemEvent.Finish e) {
            FOOD_EATEN.post(e);
        }

        public static void postSmelted(PlayerEvent.ItemSmeltedEvent e) {
            SMELTED.post(e);
        }

        public static void postDynamicTooltips(ItemTooltipEvent e) {
            DYNAMIC_TOOLTIPS.post(e);
        }

        public static void postEntityInteracted(PlayerInteractEvent.EntityInteract e) {
            ENTITY_INTERACTED.post(e);
        }

        public static void postFirstLeftClicked(PlayerInteractEvent.LeftClickEmpty e) {
            FIRST_LEFT_CLICKED.post(e);
        }

        public static void postFirstRightClicked(PlayerInteractEvent.RightClickEmpty e) {
            FIRST_RIGHT_CLICKED.post(e);
        }

        public static void postItemModification(ItemModificationEvent e) {
            ITEM_MODIFICATION.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postPickedUp);
            bus.addListener(Internal::postDestroyed);
            bus.addListener(Internal::postRightClicked);
            bus.addListener(Internal::postCrafted);
            bus.addListener(Internal::postDropped);
            bus.addListener(Internal::postFoodEaten);
            bus.addListener(Internal::postSmelted);
            bus.addListener(Internal::postDynamicTooltips);
            bus.addListener(Internal::postEntityInteracted);
            bus.addListener(Internal::postFirstLeftClicked);
            bus.addListener(Internal::postFirstRightClicked);
            bus.addListener(Internal::postItemModification);
        }

        public static void clear() {
            PICKED_UP.clear();
            DESTROYED.clear();
            RIGHT_CLICKED.clear();
            CRAFTED.clear();
            DROPPED.clear();
            FOOD_EATEN.clear();
            SMELTED.clear();
            DYNAMIC_TOOLTIPS.clear();
            ENTITY_INTERACTED.clear();
            FIRST_LEFT_CLICKED.clear();
            FIRST_RIGHT_CLICKED.clear();
            ITEM_MODIFICATION.clear();
        }

        static {
            Common.registerEventClear(Internal::clear);
        }
    }
}
