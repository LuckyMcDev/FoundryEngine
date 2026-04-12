package de.luckymcdev.foundryengine.api.event.registry;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.*;
import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {
    static final Map<Identifier, ParticleBuilderImpl> PARTICLE_BUILDERS = new LinkedHashMap<>();
    private static final Set<IEventBus> PROVIDER_LISTENERS = Collections.newSetFromMap(new IdentityHashMap<>());

    private final RegisterEvent inner;
    private final IEventBus modBus;

    public RegistryEvent(RegisterEvent inner, IEventBus modBus) {
        this.inner = inner;
        this.modBus = modBus;
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

    public void particles(ParticleBuilder... builders) {
        inner.register(BuiltInRegistries.PARTICLE_TYPE.key(), registry -> {
            for (ParticleBuilder builder : builders) {
                builder.register(registry);
                if (builder instanceof ParticleBuilderImpl impl) {
                    PARTICLE_BUILDERS.put(impl.state.id, impl);
                }
            }
        });

        registerParticleProvidersListener();
    }

    public void sounds(SoundBuilder... builders) {
        inner.register(BuiltInRegistries.SOUND_EVENT.key(), registry -> {
            for (SoundBuilder builder : builders) builder.register(registry);
        });
    }

    private void registerParticleProvidersListener() {
        if (modBus == null || PROVIDER_LISTENERS.contains(modBus)) {
            return;
        }
        PROVIDER_LISTENERS.add(modBus);

        // RegistryEventClient must never be referenced directly here as an import —
        // we load it by name so the server classloader never touches the client class.
        if (FMLEnvironment.getDist().isClient()) {
            RegistryEventClient.registerListener(modBus);
        }
    }

    public <T> void register(ResourceKey<Registry<T>> key, Consumer<RegisterEvent.RegisterHelper<T>> helper) {
        inner.register(key, helper);
    }
}