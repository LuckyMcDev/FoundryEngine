package de.luckymcdev.foundryengine.api.builder.item;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilderImpl;
import de.luckymcdev.foundryengine.common.world.item.EngineItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Builder interface for creating and customizing Items.
 * Provides a fluent API for item registration with NeoForge.
 */
public interface ItemBuilder extends BuilderBase<Item> {

    /**
     * Creates a new ItemBuilder instance.
     *
     * @param id The identifier for this item
     * @return A new ItemBuilder
     */
    static ItemBuilder create(Identifier id) {
        return new ItemBuilderImpl(id);
    }

    /**
     * Sets a custom factory function for creating the item.
     * Use this to create custom Item subclasses.
     *
     * @param factory Function that takes Item.Properties and returns an Item
     * @return This builder for chaining
     */
    ItemBuilder factory(Function<Item.Properties, Item> factory);

    /**
     * Directly modifies the Minecraft Item.Properties.
     *
     * @param propertiesAction Function to modify the properties
     * @return This builder for chaining
     */
    ItemBuilder properties(UnaryOperator<Item.Properties> propertiesAction);

    /**
     * Sets the maximum stack size for this item.
     *
     * @param count The maximum stack size (1-99)
     * @return This builder for chaining
     */
    ItemBuilder stacksTo(int count);

    /**
     * Makes this item immune to fire and lava damage.
     *
     * @return This builder for chaining
     */
    ItemBuilder fireResistant();

    /**
     * Called every tick while this item is actively being used by a living entity.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder onUseTick(EngineItem.OnUseTickCallback cb);

    /**
     * Called when this item is used on a block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder useOn(EngineItem.UseOnCallback cb);

    /**
     * Called when a player right-clicks with this item.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder use(EngineItem.UseCallback cb);

    /**
     * Called when this item finishes being used, for example after eating food.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder finishUsingItem(EngineItem.FinishUsingItemCallback cb);

    /**
     * Called when this item is used to hit an entity.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder hurtEnemy(EngineItem.HurtEnemyCallback cb);

    /**
     * Called after this item has hit an entity, for applying post-hit effects.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder postHurtEnemy(EngineItem.PostHurtEnemyCallback cb);

    /**
     * Called every tick while this item is in an inventory slot.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder inventoryTick(EngineItem.InventoryTickCallback cb);

    /**
     * Called after this item has been crafted or smelted.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder onCraftedPostProcess(EngineItem.OnCraftedPostProcessCallback cb);

    /**
     * Called when the player releases the use key before the item finishes being used.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    ItemBuilder releaseUsing(EngineItem.ReleaseUsingCallback cb);

    /**
     * Adds a data component to this item.
     *
     * @param type  The component type
     * @param value The component value
     * @param <T>   The type of the component value
     * @return This builder for chaining
     */
    <T> ItemBuilder component(DataComponentType<T> type, T value);

    /**
     * Adds a data component to this item by string type key.
     *
     * @param type  The component type as a string
     * @param value The component value
     * @param <T>   The type of the component value
     * @return This builder for chaining
     */
    <T> ItemBuilder component(String type, T value);

    /**
     * Registers this item using the provided helper.
     *
     * @param helper The register event helper
     * @return The registered Item instance
     */
    @ApiStatus.Internal
    Item register(RegisterEvent.RegisterHelper<Item> helper);
}