package de.luckymcdev.foundryengine.api.builder.block;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilderImpl;
import de.luckymcdev.foundryengine.common.world.block.EngineBlock;
import de.luckymcdev.foundryengine.common.world.item.EngineItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Builder interface for creating and customizing Blocks.
 * Provides a fluent API for block registration with NeoForge.
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
     *
     * @return This builder for chaining
     */
    BlockBuilder noItem();

    /**
     * Returns whether this block will automatically create a BlockItem.
     *
     * @return true if it creates an item, false if not
     */
    boolean hasItem();

    /**
     * Customizes the properties of the automatically created BlockItem.
     *
     * @param action Function to modify the item properties
     * @return This builder for chaining
     */
    BlockBuilder itemProperties(UnaryOperator<Item.Properties> action);

    /**
     * Called every client tick for visual effects like particles.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder animateTick(EngineBlock.AnimateTickCallback cb);

    /**
     * Called when the block is destroyed in the world.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder destroy(EngineBlock.DestroyCallback cb);

    /**
     * Called after this block is affected by an explosion.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder wasExploded(EngineBlock.WasExplodedCallback cb);

    /**
     * Called when an entity walks over this block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder stepOn(EngineBlock.StepOnCallback cb);

    /**
     * Called after a living entity places this block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder setPlacedBy(EngineBlock.SetPlacedByCallback cb);

    /**
     * Called when an entity falls onto this block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder fallOn(EngineBlock.FallOnCallback cb);

    /**
     * Called just before a player breaks this block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder playerWillDestroy(EngineBlock.PlayerWillDestroyCallback cb);

    /**
     * Called after a player breaks this block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder playerDestroy(EngineBlock.PlayerDestroyCallback cb);

    /**
     * Called when precipitation (rain/snow) hits this block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder handlePrecipitation(EngineBlock.HandlePrecipitationCallback cb);

    /**
     * Called every tick while this block's item is in an inventory.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemInventoryTick(EngineItem.InventoryTickCallback cb);

    /**
     * Called when a player right-clicks with this block's item.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemUse(EngineItem.UseCallback cb);

    /**
     * Called when this block's item is used on a block.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemUseOn(EngineItem.UseOnCallback cb);

    /**
     * Called after this block's item finishes being used.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemFinishUsing(EngineItem.FinishUsingItemCallback cb);

    /**
     * Called when this block's item hits an entity.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemHurtEnemy(EngineItem.HurtEnemyCallback cb);

    /**
     * Called after this block's item hits an entity.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemPostHurtEnemy(EngineItem.PostHurtEnemyCallback cb);

    /**
     * Called when the player releases use on this block's item.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemReleaseUsing(EngineItem.ReleaseUsingCallback cb);

    /**
     * Called after this block's item is crafted.
     *
     * @param cb The callback to invoke
     * @return This builder for chaining
     */
    BlockBuilder itemOnCraftedPostProcess(EngineItem.OnCraftedPostProcessCallback cb);

    /**
     * Registers the Block into the block registry.
     * Must be called before {@link #registerItem(RegisterEvent.RegisterHelper)} if you want a BlockItem.
     *
     * @param helper The register event helper for blocks
     * @return The registered Block instance
     */
    @ApiStatus.Internal
    Block registerBlock(RegisterEvent.RegisterHelper<Block> helper);

    /**
     * Registers the associated BlockItem into the item registry.
     * Only works if {@link #noItem()} was not called and {@link #registerBlock(RegisterEvent.RegisterHelper)} has already been called.
     *
     * @param helper The register event helper for items
     * @return The registered Item instance
     * @throws IllegalStateException if noItem() was called or registerBlock() hasn't been called yet
     */
    @ApiStatus.Internal
    Item registerItem(RegisterEvent.RegisterHelper<Item> helper);
}