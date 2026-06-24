package de.luckymcdev.foundryengine.common.registry;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Holds custom FoundryEngine registries and deferred register setup.
 */
public class EngineRegistries {
    public static Registry<RecipeResult> RECIPES;

    /**
     * Registers all deferred registers to the mod event bus.
     */
    public static void register(IEventBus modBus) {
        if (modBus == null) return;
        Keys.RB_DEF.register(modBus);
    }

    public static class Keys {
        public static final ResourceKey<Registry<RecipeResult>> RECIPES = createRegKey("recipes");

        private static final DeferredRegister<RecipeResult> RB_DEF = createDefReg(RECIPES);

        static {
            EngineRegistries.RECIPES = RB_DEF.makeRegistry(builder ->
                    new RegistryBuilder<>(Keys.RECIPES).sync(false)
            );
        }

        private static <T> ResourceKey<Registry<T>> createRegKey(String registry) {
            return ResourceKey.createRegistryKey(Common.id(registry));
        }

        private static <T> DeferredRegister<T> createDefReg(ResourceKey<Registry<T>> regKey) {
            return DeferredRegister.create(regKey, Common.MODID);
        }
    }
}