package de.luckymcdev.foundryengine.common.event.modification;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

public class ItemModificationEvent extends Event {
    private static final Map<Item, Map<DataComponentType<?>, Object>> OVERRIDES = new IdentityHashMap<>();
    private static ModifyDefaultComponentsEvent currentEvent;

    private final Item item;

    public ItemModificationEvent(Item item) {
        this.item = item;
    }

    @ApiStatus.Internal
    public static void bind(ModifyDefaultComponentsEvent event) {
        currentEvent = event;
        OVERRIDES.clear();
    }

    @ApiStatus.Internal
    public static void flush() {
        if (currentEvent == null) return;
        OVERRIDES.forEach((item, overrides) -> {
            currentEvent.modify(item, builder -> {
                for (var entry : overrides.entrySet()) {
                    @SuppressWarnings("unchecked")
                    var type = (DataComponentType<Object>) entry.getKey();
                    builder.set(type, entry.getValue());
                }
            });
        });
        OVERRIDES.clear();
        currentEvent = null;
    }

    public Item getItem() {
        return item;
    }

    public ItemModificationEvent setMaxStackSize(int size) {
        overrides().put(DataComponents.MAX_STACK_SIZE, size);
        return this;
    }

    public ItemModificationEvent setMaxDamage(int damage) {
        overrides().put(DataComponents.MAX_DAMAGE, damage);
        return this;
    }

    public ItemModificationEvent setUnbreakable() {
        overrides().put(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        return this;
    }

    public ItemModificationEvent setFood(FoodProperties food) {
        overrides().put(DataComponents.FOOD, food);
        return this;
    }

    public ItemModificationEvent setTool(Tool tool) {
        overrides().put(DataComponents.TOOL, tool);
        return this;
    }

    public ItemModificationEvent setAttributeModifiers(ItemAttributeModifiers modifiers) {
        overrides().put(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        return this;
    }

    private Map<DataComponentType<?>, Object> overrides() {
        return OVERRIDES.computeIfAbsent(item, k -> new IdentityHashMap<>());
    }
}
