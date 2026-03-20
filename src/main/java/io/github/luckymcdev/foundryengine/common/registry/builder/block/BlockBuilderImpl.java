package io.github.luckymcdev.foundryengine.common.registry.builder.block;

import io.github.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import io.github.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import io.github.luckymcdev.foundryengine.common.registry.builder.BuilderBaseImpl;
import io.github.luckymcdev.foundryengine.common.registry.builder.item.ItemBuilderImpl;
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
import java.util.function.UnaryOperator;

/**
 * A Builder for registering a block in the {@link net.neoforged.neoforge.registries.RegisterEvent}
 * or anywhere else, where you can register a block easily
 */
public class BlockBuilderImpl extends BuilderBaseImpl<Block> implements BlockBuilder {
    private final BiFunction<Block, Item.Properties, Item> itemFactory;
    private BlockBehaviour.Properties properties;
    private Function<BlockBehaviour.Properties, Block> blockFactory;
    private boolean hasItem = true;
    private UnaryOperator<Item.Properties> itemPropertyModifier = p -> p;

    public BlockBuilderImpl(Identifier id) {
        super(id);
        this.registryKey = Registries.BLOCK;
        this.properties = BlockBehaviour.Properties.of();
        this.blockFactory = Block::new;
        this.itemFactory = BlockItem::new;
    }

    @Override
    public BlockBuilder factory(Function<BlockBehaviour.Properties, Block> factory) {
        this.blockFactory = factory;
        return this;
    }

    @Override
    public BlockBuilder properties(UnaryOperator<BlockBehaviour.Properties> action) {
        this.properties = action.apply(this.properties);
        return this;
    }

    @Override
    public BlockBuilder noItem() {
        this.hasItem = false;
        return this;
    }

    @Override
    public BlockBuilder itemProperties(UnaryOperator<Item.Properties> action) {
        this.itemPropertyModifier = action;
        return this;
    }

    @Override
    public Block registerBlock(RegisterEvent.RegisterHelper<Block> helper) {
        Block block = build();
        helper.register(this.id, block);
        this.object = block;
        return block;
    }

    @Override
    public Item registerItem(RegisterEvent.RegisterHelper<Item> helper) {
        if (!hasItem) {
            throw new IllegalStateException("Cannot register item for block " + id + " because noItem() was called.");
        }

        ItemBuilder itemBuilder = new ItemBuilderImpl(id)
                .factory(props -> itemFactory.apply(object, props))
                .properties(itemPropertyModifier);

        return itemBuilder.register(helper);
    }

    @Override
    public Block build() {
        this.properties.setId(ResourceKey.create(Registries.BLOCK, id));
        return blockFactory.apply(this.properties);
    }
}