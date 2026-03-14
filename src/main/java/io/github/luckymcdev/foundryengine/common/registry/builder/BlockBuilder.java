package io.github.luckymcdev.foundryengine.common.registry.builder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A Builder for registering a block in the {@link net.neoforged.neoforge.registries.RegisterEvent}
 * or anywhere else, where you can register a block easily
 */
public class BlockBuilder extends BuilderBase<Block> {
    private final BiFunction<Block, Item.Properties, Item> itemFactory;
    private BlockBehaviour.Properties properties;
    private Function<BlockBehaviour.Properties, Block> blockFactory;
    private boolean hasItem = true;
    private Function<Item.Properties, Item.Properties> itemPropertyModifier = p -> p;

    public BlockBuilder(Identifier id) {
        super(id);
        this.registryKey = Registries.BLOCK;
        this.properties = BlockBehaviour.Properties.of();
        this.blockFactory = Block::new;
        this.itemFactory = BlockItem::new;
    }

    public BlockBuilder factory(Function<BlockBehaviour.Properties, Block> factory) {
        this.blockFactory = factory;
        return this;
    }

    public BlockBuilder properties(Function<BlockBehaviour.Properties, BlockBehaviour.Properties> action) {
        this.properties = action.apply(this.properties);
        return this;
    }

    /**
     * Disables the automatic creation of a BlockItem for this block.
     */
    public BlockBuilder noItem() {
        this.hasItem = false;
        return this;
    }

    /**
     * Customizes the BlockItem properties (e.g., set stack size or rarity).
     */
    public BlockBuilder itemProperties(Function<Item.Properties, Item.Properties> action) {
        this.itemPropertyModifier = action;
        return this;
    }

    /**
     * Registers the Block into the block registry.
     * MUST be called BEFORE {@link #registerItem(RegisterEvent.RegisterHelper)}
     */
    public Block registerBlock(RegisterEvent.RegisterHelper<Block> helper) {
        Block block = build();
        helper.register(this.id, block);
        this.object = block;
        return block;
    }

    /**
     * Registers the associated BlockItem into the item registry.
     * Only works if hasItem is true and if {@link #registerBlock(RegisterEvent.RegisterHelper)} has been called.
     */
    public Item registerItem(RegisterEvent.RegisterHelper<Item> helper) {
        if (!hasItem) {
            throw new IllegalStateException("Cannot register item for block " + id + " because noItem() was called.");
        }

        ItemBuilder itemBuilder = new ItemBuilder(id)
                .factory(props -> itemFactory.apply(object, props))
                .properties(itemPropertyModifier);

        return itemBuilder.register(helper);
    }

    @Override
    public Block build() {
        this.properties.setId(ResourceKey.create(Registries.BLOCK, id));
        return blockFactory.apply(this.properties);
    }

    @Override
    public Block transformObject(Block block) {
        if (hasItem) {
            ItemBuilder itemBuilder = new ItemBuilder(id)
                    .factory(props -> itemFactory.apply(block, props))
                    .properties(itemPropertyModifier);
        }
        return block;
    }
}