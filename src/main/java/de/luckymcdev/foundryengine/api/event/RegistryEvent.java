package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.client.particle.EngineParticle;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryEvent.class);
    private static final Set<IEventBus> PROVIDER_LISTENERS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<Identifier, ParticleBuilderImpl> PARTICLE_BUILDERS = new LinkedHashMap<>();

    private final RegisterEvent inner;
    private final IEventBus modBus;

    public RegistryEvent(RegisterEvent inner, IEventBus modBus) {
        this.inner = inner;
        this.modBus = modBus;
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        for (ParticleBuilderImpl builder : List.copyOf(PARTICLE_BUILDERS.values())) {
            ParticleType<?> type = builder.get();
            if (type instanceof SimpleParticleType simpleType) {
                event.registerSpriteSet(simpleType, (SpriteSet sprites) ->
                        new EngineParticle.Provider(builder.id, builder.getLifetime(), builder.getLayer(), builder.mergedData(), sprites)
                );
            } else {
                LOGGER.warn("Skipping particle provider registration for {} because type {} is not SimpleParticleType.",
                        builder.id,
                        type.getClass().getName());
            }
        }
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
                    PARTICLE_BUILDERS.put(impl.id, impl);
                }
            }
        });

        registerParticleProvidersListener();
    }

    private void registerParticleProvidersListener() {
        if (modBus == null || PROVIDER_LISTENERS.contains(modBus)) {
            return;
        }
        PROVIDER_LISTENERS.add(modBus);
        modBus.addListener(RegistryEvent::registerParticleProviders);
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
