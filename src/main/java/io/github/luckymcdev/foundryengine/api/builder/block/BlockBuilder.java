package io.github.luckymcdev.foundryengine.api.builder.block;

import io.github.luckymcdev.foundryengine.api.builder.BuilderBase;
import io.github.luckymcdev.foundryengine.common.registry.builder.block.BlockBuilderImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Builder interface for creating and customizing Blocks.
 * Provides a fluent API for block registration with NeoForge.
 * <p>
 * Blocks can optionally create an associated BlockItem automatically.
 */
public interface BlockBuilder extends BuilderBase<Block> {

    /**
     * Creates a new BlockBuilder instance.
     *
     * @param id The identifier for this block
     * @return A new BlockBuilder
     */
    static BlockBuilder create(Identifier id) {
        return new BlockBuilderImpl(id);
    }

    /**
     * Sets a custom factory function for creating the block.
     * Use this to create custom Block subclasses.
     *
     * @param factory Function that takes BlockBehaviour.Properties and returns a Block
     * @return This builder for chaining
     */
    BlockBuilder factory(Function<BlockBehaviour.Properties, Block> factory);

    /**
     * Modifies the block properties (material, hardness, resistance, etc.).
     *
     * @param action Function to modify the properties
     * @return This builder for chaining
     */
    BlockBuilder properties(UnaryOperator<BlockBehaviour.Properties> action);

    /**
     * Disables the automatic creation of a BlockItem for this block.
     * Use this for blocks that shouldn't appear in the inventory.
     *
     * @return This builder for chaining
     */
    BlockBuilder noItem();

    /**
     * Customizes the properties of the automatically created BlockItem.
     * For example, to set stack size or rarity.
     *
     * @param action Function to modify the item properties
     * @return This builder for chaining
     */
    BlockBuilder itemProperties(UnaryOperator<Item.Properties> action);

    /**
     * Registers the Block into the block registry.
     * MUST be called BEFORE {@link #registerItem(RegisterEvent.RegisterHelper)} if you want a BlockItem.
     *
     * @param helper The register event helper for blocks
     * @return The registered Block instance
     */
    Block registerBlock(RegisterEvent.RegisterHelper<Block> helper);

    /**
     * Registers the associated BlockItem into the item registry.
     * Only works if {@link #noItem()} was NOT called and if {@link #registerBlock(RegisterEvent.RegisterHelper)}
     * has already been called.
     *
     * @param helper The register event helper for items
     * @return The registered Item instance
     * @throws IllegalStateException if noItem() was called or if registerBlock() hasn't been called yet
     */
    Item registerItem(RegisterEvent.RegisterHelper<Item> helper);
}