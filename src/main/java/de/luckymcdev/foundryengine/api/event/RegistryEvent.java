package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {

    private final RegisterEvent inner;

    public RegistryEvent(RegisterEvent inner) {
        this.inner = inner;
    }

    public void items(ItemBuilder... builders) {
        inner.register(BuiltInRegistries.ITEM.key(), registry -> {
            for (ItemBuilder builder : builders) builder.register(registry);
        });
    }

    public void blocks(BlockBuilder... builders) {
        inner.register(BuiltInRegistries.BLOCK.key(), registry -> {
            for (BlockBuilder builder : builders) builder.registerBlock(registry);
        });
        inner.register(BuiltInRegistries.ITEM.key(), registry -> {
            for (BlockBuilder builder : builders) {
                if (builder.hasItem()) builder.registerItem(registry);
            }
        });
    }

    public void recipes(RecipeBuilder... builders) {
        inner.register(EngineRegistries.RECIPES.key(), registry -> {
            for (RecipeBuilder builder : builders) builder.register(registry);
        });
    }

    /**
     * Use {@link BuiltInRegistries} for the key.
     * All keys are available by calling {@code key()} on the respective registry.
     *
     * @param key    the Resource Key of the Registry youre trying to register to.
     * @param helper the Registry helper you can use to register something.
     * @param <T>    The type of registry.
     */
    public <T> void register(ResourceKey<Registry<T>> key, Consumer<RegisterEvent.RegisterHelper<T>> helper) {
        inner.register(key, helper);
    }
}