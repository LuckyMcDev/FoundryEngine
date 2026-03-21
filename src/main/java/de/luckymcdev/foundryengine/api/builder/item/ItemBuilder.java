package de.luckymcdev.foundryengine.api.builder.item;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.registry.builder.item.ItemBuilderImpl;
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
     * Adds a data component to this item.
     *
     * @param type  The component type
     * @param value The component value
     * @param <T>   The type of the component value
     * @return This builder for chaining
     */
    <T> ItemBuilder component(DataComponentType<T> type, T value);

    /**
     * Registers this item using the provided helper.
     *
     * @param helper The register event helper
     * @return The registered Item instance
     */
    @ApiStatus.Internal
    Item register(RegisterEvent.RegisterHelper<Item> helper);
}