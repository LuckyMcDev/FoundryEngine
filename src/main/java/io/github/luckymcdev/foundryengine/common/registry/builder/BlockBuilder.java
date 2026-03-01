package io.github.luckymcdev.foundryengine.common.registry.builder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BlockBuilder extends BuilderBase<Block> {
    private BlockBehaviour.Properties properties;
    private Function<BlockBehaviour.Properties, Block> blockFactory;
    private boolean hasItem = true;
    private final BiFunction<Block, Item.Properties, Item> itemFactory;
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