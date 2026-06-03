package de.luckymcdev.foundryengine.api.event.registry;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilderImpl;
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
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {
    static final Map<Identifier, ParticleBuilderImpl> PARTICLE_BUILDERS = new LinkedHashMap<>();
    static final Map<Identifier, SoundBuilderImpl> SOUND_BUILDERS = new LinkedHashMap<>();
    static final Map<Identifier, ItemBuilder> ITEM_BUILDERS = new LinkedHashMap<>();
    static final Map<Identifier, BlockBuilder> BLOCK_BUILDERS = new LinkedHashMap<>();
    static final Map<Identifier, RecipeBuilder> RECIPE_BUILDERS = new LinkedHashMap<>();
    private static final Set<IEventBus> PROVIDER_LISTENERS = Collections.newSetFromMap(new IdentityHashMap<>());

    private final RegisterEvent inner;
    private final IEventBus modBus;

    public RegistryEvent(RegisterEvent inner, IEventBus modBus) {
        this.inner = inner;
        this.modBus = modBus;
    }

    @Nullable
    public static SoundBuilderImpl getSoundBuilder(Identifier id) {
        return SOUND_BUILDERS.get(id);
    }

    public static Collection<ItemBuilder> getItemBuilders() {
        return Collections.unmodifiableCollection(ITEM_BUILDERS.values());
    }

    public static Collection<BlockBuilder> getBlockBuilders() {
        return Collections.unmodifiableCollection(BLOCK_BUILDERS.values());
    }

    public static Collection<RecipeBuilder> getRecipeBuilders() {
        return Collections.unmodifiableCollection(RECIPE_BUILDERS.values());
    }

    public static Collection<ParticleBuilderImpl> getParticleBuilders() {
        return Collections.unmodifiableCollection(PARTICLE_BUILDERS.values());
    }

    public static Collection<SoundBuilderImpl> getSoundBuilders() {
        return Collections.unmodifiableCollection(SOUND_BUILDERS.values());
    }

    public void items(ItemBuilder... builders) {
        inner.register(BuiltInRegistries.ITEM.key(), registry -> {
            for (ItemBuilder builder : builders) {
                builder.register(registry);
                ITEM_BUILDERS.put(builder.getId(), builder);
            }
        });
    }

    public void blocks(BlockBuilder... builders) {
        inner.register(BuiltInRegistries.BLOCK.key(), registry -> {
            for (BlockBuilder builder : builders) {
                builder.registerBlock(registry);
                BLOCK_BUILDERS.put(builder.getId(), builder);
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
                RECIPE_BUILDERS.put(builder.getId(), builder);
            }
        });
    }

    public void particles(ParticleBuilder... builders) {
        inner.register(BuiltInRegistries.PARTICLE_TYPE.key(), registry -> {
            for (ParticleBuilder builder : builders) {
                builder.register(registry);
                if (builder instanceof ParticleBuilderImpl impl) {
                    PARTICLE_BUILDERS.put(impl.getId(), impl);
                }
            }
        });

        registerParticleProvidersListener();
    }

    public void sounds(SoundBuilder... builders) {
        inner.register(BuiltInRegistries.SOUND_EVENT.key(), registry -> {
            for (SoundBuilder builder : builders) {
                builder.register(registry);
                var impl = (SoundBuilderImpl) builder;
                SOUND_BUILDERS.put(impl.getId(), impl);
            }
        });
    }

    private void registerParticleProvidersListener() {
        if (modBus == null || PROVIDER_LISTENERS.contains(modBus)) {
            return;
        }
        PROVIDER_LISTENERS.add(modBus);
        if (FMLEnvironment.getDist().isClient()) {
            RegistryEventClient.registerListener(modBus);
        }
    }

    public <T> void register(ResourceKey<Registry<T>> key, Consumer<RegisterEvent.RegisterHelper<T>> helper) {
        inner.register(key, helper);
    }
}