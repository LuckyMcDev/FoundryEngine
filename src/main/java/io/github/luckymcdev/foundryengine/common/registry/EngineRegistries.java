package io.github.luckymcdev.foundryengine.common.registry;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.registry.builder.RecipeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class EngineRegistries {
    public static final Registry<RecipeBuilder> RECIPE_BUILDERS;

    static {
        RECIPE_BUILDERS = (new RegistryBuilder<>(Keys.RECIPE_BUILDERS)).sync(true).create();
    }

    public static final class Keys {
        public static final ResourceKey<Registry<RecipeBuilder>> RECIPE_BUILDERS = key("recipe_builders");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(Common.id(name));
        }
    }
}
