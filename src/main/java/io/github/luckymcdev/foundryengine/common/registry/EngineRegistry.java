package io.github.luckymcdev.foundryengine.common.registry;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.registry.builder.RecipeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class EngineRegistry {
    public static final ResourceKey<Registry<RecipeBuilder>> RECIPE_BUILDERS_KEY =
            ResourceKey.createRegistryKey(Common.id("recipe_builders"));

    public static final Registry<RecipeBuilder> RECIPE_BUILDERS;

    private static final DeferredRegister<RecipeBuilder> INTERNAL =
            DeferredRegister.create(EngineRegistry.RECIPE_BUILDERS_KEY, "foundryengine");


    static {
        RECIPE_BUILDERS = INTERNAL.makeRegistry(builder ->
                new RegistryBuilder<>(RECIPE_BUILDERS_KEY).sync(true)
        );
    }

    public static void register(IEventBus modBus) {
        INTERNAL.register(modBus);
    }
}
