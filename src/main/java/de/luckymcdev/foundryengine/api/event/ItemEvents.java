package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.*;
import org.jetbrains.annotations.ApiStatus;

public class ItemEvents {
    public static final EventGroupHolder<ItemEntityPickupEvent.Post> PICKED_UP =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_PICKUP, BlueprintContexts::itemPickedUp);
    public static final EventGroupHolder<PlayerDestroyItemEvent> DESTROYED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_DESTROY, BlueprintContexts::itemDestroyed);
    public static final EventGroupHolder<PlayerInteractEvent.RightClickItem> RIGHT_CLICKED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_RIGHT_CLICK, BlueprintContexts::itemRightClicked);
    public static final EventGroupHolder<PlayerEvent.ItemCraftedEvent> CRAFTED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_CRAFTED, BlueprintContexts::itemCrafted);
    public static final EventGroupHolder<ItemTossEvent> DROPPED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_DROPPED, BlueprintContexts::itemDropped);
    public static final EventGroupHolder<LivingEntityUseItemEvent.Finish> FOOD_EATEN =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_FOOD_EATEN, BlueprintContexts::foodEaten);
    public static final EventGroupHolder<PlayerEvent.ItemSmeltedEvent> SMELTED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_SMELTED, BlueprintContexts::itemSmelted);
    public static final EventGroupHolder<ItemTooltipEvent> DYNAMIC_TOOLTIPS =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_TOOLTIP, BlueprintContexts::itemTooltip);
    public static final EventGroupHolder<PlayerInteractEvent.EntityInteract> ENTITY_INTERACTED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_ENTITY_INTERACT, BlueprintContexts::itemEntityInteract);
    public static final EventGroupHolder<PlayerInteractEvent.LeftClickEmpty> FIRST_LEFT_CLICKED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_FIRST_LEFT_CLICK, BlueprintContexts::firstLeftClicked);
    public static final EventGroupHolder<PlayerInteractEvent.RightClickEmpty> FIRST_RIGHT_CLICKED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ITEM_FIRST_RIGHT_CLICK, BlueprintContexts::firstRightClicked);

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

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(PICKED_UP::post);
            bus.addListener(DESTROYED::post);
            bus.addListener(RIGHT_CLICKED::post);
            bus.addListener(CRAFTED::post);
            bus.addListener(DROPPED::post);
            bus.addListener(FOOD_EATEN::post);
            bus.addListener(SMELTED::post);
            bus.addListener(DYNAMIC_TOOLTIPS::post);
            bus.addListener(ENTITY_INTERACTED::post);
            bus.addListener(FIRST_LEFT_CLICKED::post);
            bus.addListener(FIRST_RIGHT_CLICKED::post);
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
        }
    }
}