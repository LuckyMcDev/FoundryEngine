package de.luckymcdev.foundryengine.common.event.registry;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {
    private final RegisterEvent inner;
    private final RegistryCollector collector;

    public RegistryEvent(RegisterEvent inner, RegistryCollector collector) {
        this.inner = inner;
        this.collector = collector;
    }

    public void items(ItemBuilder... builders) {
        registerEach(BuiltInRegistries.ITEM, ItemBuilder::register, collector::addItem, List.of(builders));
    }

    public void blocks(BlockBuilder... builders) {
        var list = List.of(builders);
        registerEach(BuiltInRegistries.BLOCK, BlockBuilder::registerBlock, collector::addBlock, list);
        registerEach(BuiltInRegistries.ITEM, BlockBuilder::registerItem, null, list.stream().filter(BlockBuilder::hasItem).toList());
    }

    public void recipes(RecipeBuilder... builders) {
        for (RecipeBuilder builder : builders) {
            collector.addRecipe(builder);
        }
    }

    public void particles(ParticleBuilder... builders) {
        registerEach(BuiltInRegistries.PARTICLE_TYPE, ParticleBuilder::register, collector::addParticle, List.of(builders));
    }

    public void sounds(SoundBuilder... builders) {
        registerEach(BuiltInRegistries.SOUND_EVENT, SoundBuilder::register, collector::addSound, List.of(builders));
    }

    private <T, B> void registerEach(Registry<T> registry, BiConsumer<B, RegisterEvent.RegisterHelper<T>> registrator,
                                      @Nullable Consumer<B> collector, List<B> builders) {
        inner.register(registry.key(), helper -> {
            for (B builder : builders) {
                registrator.accept(builder, helper);
                if (collector != null) collector.accept(builder);
            }
        });
    }

    public <T> void register(ResourceKey<Registry<T>> key, Consumer<RegisterEvent.RegisterHelper<T>> helper) {
        inner.register(key, helper);
    }
}
