package de.luckymcdev.foundryengine.common.event.registry;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {
    private final RegisterEvent inner;
    private final RegistryCollector collector;

    public RegistryEvent(RegisterEvent inner, RegistryCollector collector) {
        this.inner = inner;
        this.collector = collector;
    }

    public void items(ItemBuilder... builders) {
        inner.register(BuiltInRegistries.ITEM.key(), registry -> {
            for (ItemBuilder builder : builders) {
                builder.register(registry);
                collector.addItem(builder);
            }
        });
    }

    public void blocks(BlockBuilder... builders) {
        inner.register(BuiltInRegistries.BLOCK.key(), registry -> {
            for (BlockBuilder builder : builders) {
                builder.registerBlock(registry);
                collector.addBlock(builder);
            }
        });
        inner.register(BuiltInRegistries.ITEM.key(), registry -> {
            for (BlockBuilder builder : builders) {
                if (builder.hasItem()) builder.registerItem(registry);
            }
        });
    }

    public void recipes(RecipeBuilder... builders) {
        inner.register(EngineRegistries.RECIPES.key(), registry -> {
            for (RecipeBuilder builder : builders) {
                builder.register(registry);
                collector.addRecipe(builder);
            }
        });
    }

    public void particles(ParticleBuilder... builders) {
        inner.register(BuiltInRegistries.PARTICLE_TYPE.key(), registry -> {
            for (ParticleBuilder builder : builders) {
                builder.register(registry);
                collector.addParticle(builder);
            }
        });
    }

    public void sounds(SoundBuilder... builders) {
        inner.register(BuiltInRegistries.SOUND_EVENT.key(), registry -> {
            for (SoundBuilder builder : builders) {
                builder.register(registry);
                collector.addSound(builder);
            }
        });
    }

    public <T> void register(ResourceKey<Registry<T>> key, Consumer<RegisterEvent.RegisterHelper<T>> helper) {
        inner.register(key, helper);
    }
}
