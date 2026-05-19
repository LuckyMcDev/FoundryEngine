package de.luckymcdev.foundryengine.common.data;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.client.*;
import de.luckymcdev.foundryengine.common.data.providers.server.EngineGlobalLootModifierProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.adv.EngineAdvancementProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.adv.EngineAdvancementSubProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.loot.EngineLootTableProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.loot.EngineLootTableSubProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.recipe.EngineRecipePrioritiesProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.recipe.EngineRecipeProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.tags.EngineBlockTagsProvider;
import de.luckymcdev.foundryengine.common.data.providers.server.tags.EngineItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


/**
 *
 * Server:
 * {@link net.minecraft.data.advancements.AdvancementProvider} / {@link net.minecraft.data.advancements.AdvancementSubProvider}
 * {@link net.minecraft.data.loot.LootTableProvider} / {@link net.minecraft.data.loot.LootTableSubProvider}
 * {@link net.minecraft.data.recipes.RecipeProvider}
 * {@link net.neoforged.neoforge.common.data.RecipePrioritiesProvider}
 * {@link net.neoforged.neoforge.common.data.BlockTagsProvider} {@link net.neoforged.neoforge.common.data.ItemTagsProvider} {@link net.minecraft.data.tags.IntrinsicHolderTagsProvider}
 * {@link net.neoforged.neoforge.common.data.GlobalLootModifierProvider}
 * {@link net.neoforged.neoforge.common.data.JsonCodecProvider}
 * <p>
 * Client:
 * {@link net.neoforged.neoforge.common.data.LanguageProvider}
 * {@link net.minecraft.client.data.models.ModelProvider}
 * {@link net.minecraft.client.data.models.EquipmentAssetProvider}
 * {@link net.neoforged.neoforge.client.data.ParticleDescriptionProvider}
 * {@link net.neoforged.neoforge.common.data.SoundDefinitionsProvider}
 */
public class BundleDataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static Path generatedDataPath;
    private static Path generatedAssetsPath;

    public static Path getGeneratedDataPath() {
        return generatedDataPath;
    }

    public static Path getGeneratedAssetsPath() {
        return generatedAssetsPath;
    }

    public static void runAll() {
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            run(bundle.info().id());
        }
    }

    public static void run(String bundleId) {
        EngineDataGenerator gen = new EngineDataGenerator(Common.TEMP_DIR.resolve("bundles"));

        LayeredRegistryAccess<RegistryLayer> layeredAccess = RegistryLayer.createRegistryAccess();
        CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.completedFuture(
                layeredAccess.compositeAccess()
        );

        PackOutput pOut = gen.getGenerator().getPackOutput();
        Path outputRoot = gen.getOutput();

        try {
            LOGGER.info(outputRoot.toAbsolutePath().toString());

            // Server
            gen.addProvider(new EngineAdvancementProvider(
                    pOut,
                    lookupProvider,
                    List.of(
                            new EngineAdvancementSubProvider()
                    )
            ));
            gen.addProvider(new EngineLootTableProvider(
                    pOut,
                    Set.of(),
                    List.of(
                            new LootTableProvider.SubProviderEntry(
                                    EngineLootTableSubProvider::new,
                                    LootContextParamSets.BLOCK
                            )
                    ),
                    lookupProvider
            ));
            gen.addProvider(new EngineRecipeProvider.Runner(pOut, lookupProvider, bundleId));
            gen.addProvider(new EngineRecipePrioritiesProvider(pOut, lookupProvider, bundleId));
            gen.addProvider(new EngineBlockTagsProvider(pOut, lookupProvider, bundleId));
            gen.addProvider(new EngineItemTagsProvider(pOut, lookupProvider, bundleId));
            gen.addProvider(new EngineGlobalLootModifierProvider(pOut, lookupProvider, bundleId));


            // Client
            gen.addProvider(new EngineLanguageProvider(pOut, bundleId, "en_us"));
            gen.addProvider(new EngineModelProvider(pOut, bundleId));
            gen.addProvider(new EngineEquipmentAssetProvider(pOut));
            gen.addProvider(new EngineParticleDescriptionProvider(pOut));
            gen.addProvider(new EngineSoundDefinitionsProvider(pOut, bundleId));


            gen.run();

            generatedDataPath = outputRoot.resolve("data");
            generatedAssetsPath = outputRoot.resolve("assets");
        } catch (IOException e) {
            LOGGER.error("Failed to run data generator: {}", e.getMessage());
        }
    }
}
