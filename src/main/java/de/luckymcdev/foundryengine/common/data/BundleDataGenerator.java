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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
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
    private static final Path OUTPUT_ROOT = Common.TEMP_DIR.resolve("instances").resolve(instanceKey()).resolve("bundles");

    private static final Path generatedDataPath = OUTPUT_ROOT.resolve("data");
    private static final Path generatedAssetsPath = OUTPUT_ROOT.resolve("assets");

    public static Path getGeneratedDataPath() {
        return generatedDataPath;
    }

    public static Path getGeneratedAssetsPath() {
        return generatedAssetsPath;
    }

    public static void runAll() {
        prepareOutputDirectories();
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            run(bundle);
        }
    }

    public static void run(Bundle bundle) {
        prepareOutputDirectories();
        EngineDataGenerator gen = new EngineDataGenerator(OUTPUT_ROOT);

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
                            new EngineAdvancementSubProvider(bundle)
                    ),
                    bundle
            ));
            gen.addProvider(new EngineLootTableProvider(
                    pOut,
                    Set.of(),
                    List.of(
                            new LootTableProvider.SubProviderEntry(
                                    registries -> new EngineLootTableSubProvider(bundle, registries),
                                    LootContextParamSets.BLOCK
                            )
                    ),
                    lookupProvider,
                    bundle
            ));
            gen.addProvider(new EngineRecipeProvider.Runner(pOut, lookupProvider, bundle));
            gen.addProvider(new EngineRecipePrioritiesProvider(pOut, lookupProvider, bundle));
            gen.addProvider(new EngineBlockTagsProvider(pOut, lookupProvider, bundle));
            gen.addProvider(new EngineItemTagsProvider(pOut, lookupProvider, bundle));
            gen.addProvider(new EngineGlobalLootModifierProvider(pOut, lookupProvider, bundle));


            // Client
            gen.addProvider(new EngineLanguageProvider(pOut,"en_us", bundle));
            gen.addProvider(new EngineModelProvider(pOut, bundle));
            gen.addProvider(new EngineEquipmentAssetProvider(pOut, bundle));
            gen.addProvider(new EngineParticleDescriptionProvider(pOut, bundle));
            gen.addProvider(new EngineSoundDefinitionsProvider(pOut, bundle));


            gen.run();
        } catch (IOException e) {
            LOGGER.error("Failed to run data generator: {}", e.getMessage());
        }
    }

    private static void prepareOutputDirectories() {
        try {
            Files.createDirectories(generatedDataPath);
            Files.createDirectories(generatedAssetsPath);
        } catch (IOException e) {
            LOGGER.error("Failed to create generated pack directories: {}", e.getMessage());
        }
    }

    private static String instanceKey() {
        String gameDir = Common.GAMEDIR.toString().toLowerCase(Locale.ROOT);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(gameDir.getBytes(StandardCharsets.UTF_8));
            StringBuilder key = new StringBuilder("game-");
            for (int i = 0; i < 8; i++) {
                key.append(String.format(Locale.ROOT, "%02x", digest[i]));
            }
            return key.toString();
        } catch (NoSuchAlgorithmException e) {
            return "game-" + Integer.toUnsignedString(gameDir.hashCode(), 16);
        }
    }
}
