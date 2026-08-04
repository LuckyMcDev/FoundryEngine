package de.luckymcdev.foundryengine.common.data;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.data.providers.EngineEquipmentAssetProvider;
import de.luckymcdev.foundryengine.client.data.providers.EngineLanguageProvider;
import de.luckymcdev.foundryengine.client.data.providers.EngineModelProvider;
import de.luckymcdev.foundryengine.client.data.providers.EngineParticleDescriptionProvider;
import de.luckymcdev.foundryengine.client.data.providers.EngineSoundDefinitionsProvider;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.tag.TagBuilder;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.BundleExceptionHandler;
import de.luckymcdev.foundryengine.common.event.data.BundleDataGenEvent;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import de.luckymcdev.foundryengine.server.data.providers.EngineGlobalLootModifierProvider;
import de.luckymcdev.foundryengine.server.data.providers.adv.EngineAdvancementProvider;
import de.luckymcdev.foundryengine.server.data.providers.adv.EngineAdvancementSubProvider;
import de.luckymcdev.foundryengine.server.data.providers.loot.EngineLootTableProvider;
import de.luckymcdev.foundryengine.server.data.providers.loot.EngineLootTableSubProvider;
import de.luckymcdev.foundryengine.server.data.providers.recipe.EngineRecipePrioritiesProvider;
import de.luckymcdev.foundryengine.server.data.providers.recipe.EngineRecipeProvider;
import de.luckymcdev.foundryengine.server.data.providers.tags.EngineBlockTagsProvider;
import de.luckymcdev.foundryengine.server.data.providers.tags.EngineItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.io.FileUtils;
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
 * Generates data (recipes, loot tables, models, etc.) for all loaded bundles.
 */
public class BundleDataGenerator {
	public static final Path OUTPUT_ROOT = Common.TEMP_DIR.resolve("instances").resolve(instanceKey()).resolve("bundles");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path generatedDataPath = OUTPUT_ROOT.resolve("data");
	private static final Path generatedAssetsPath = OUTPUT_ROOT.resolve("assets");

	/**
	 * Returns the path where generated data files are placed.
	 */
	public static Path getGeneratedDataPath() {
		return generatedDataPath;
	}

	/**
	 * Returns the path where generated asset files are placed.
	 */
	public static Path getGeneratedAssetsPath() {
		return generatedAssetsPath;
	}

	/**
	 * Runs data generation for all loaded bundles.
	 */
	public static void runAll() {
		prepareOutputDirectories();
		for (Bundle bundle : Common.getBundleManager().getBundles()) {
			run(bundle);
		}
		try {
			LOGGER.info("Custom data generator running");
			EngineDataGenerator customGen = new EngineDataGenerator(OUTPUT_ROOT);
			LayeredRegistryAccess<RegistryLayer> layeredAccess = RegistryLayer.createRegistryAccess();
			CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.completedFuture(
				layeredAccess.compositeAccess()
			);
			NeoForge.EVENT_BUS.post(new BundleDataGenEvent(customGen, lookupProvider));
			customGen.run();
		} catch (IOException e) {
			BundleExceptionHandler.handle("Custom Data Generator Crashed.", e);
		}
	}

	/**
	 * Runs data generation for a single bundle.
	 */
	public static void run(Bundle bundle) {
		prepareOutputDirectories();
		EngineDataGenerator gen = new EngineDataGenerator(OUTPUT_ROOT);

		LayeredRegistryAccess<RegistryLayer> layeredAccess = RegistryLayer.createRegistryAccess();
		CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.completedFuture(
			layeredAccess.compositeAccess()
		);

		RegistryCollector collector = Common.getRegistryCollector();
		if (collector == null) {
			return;
		}

		PackOutput pOut = gen.getGenerator().getPackOutput();
		Path outputRoot = gen.getOutput();
		String namespace = bundle.info().id();

		List<BlockBuilder> blockBuilders = collector.getBlocks().stream()
			.filter(b -> b.getId().getNamespace().equals(namespace) && b.shouldGenerateData())
			.toList();

		List<ItemBuilder> itemBuilders = collector.getItems().stream()
			.filter(b -> b.getId().getNamespace().equals(namespace) && b.shouldGenerateData())
			.toList();

		List<RecipeBuilder> recipeBuilders = collector.getRecipes().stream()
			.filter(b -> b.getId().getNamespace().equals(namespace) && b.shouldGenerateData())
			.toList();

		List<SoundBuilder> soundBuilders = collector.getSounds().stream()
			.filter(b -> b.getId().getNamespace().equals(namespace) && b.shouldGenerateData())
			.toList();

		List<TagBuilder<?>> tagBuilders = collector.getTags().stream()
			.filter(b -> b.getId().getNamespace().equals(namespace) && b.shouldGenerateData())
			.toList();

		try {
			LOGGER.info(outputRoot.toAbsolutePath().toString());

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
						registries -> new EngineLootTableSubProvider(registries, blockBuilders),
						LootContextParamSets.BLOCK
					)
				),
				lookupProvider
			));
			gen.addProvider(new EngineRecipeProvider.Runner(pOut, lookupProvider, namespace, recipeBuilders));
			gen.addProvider(new EngineRecipePrioritiesProvider(pOut, lookupProvider, namespace));
			var blockTagBuilders = tagBuilders.stream()
				.filter(b -> b.registry().equals(net.minecraft.core.registries.Registries.BLOCK))
				.toList();
			var itemTagBuilders = tagBuilders.stream()
				.filter(b -> b.registry().equals(net.minecraft.core.registries.Registries.ITEM))
				.toList();

			gen.addProvider(new EngineBlockTagsProvider(pOut, lookupProvider, namespace, blockTagBuilders));
			gen.addProvider(new EngineItemTagsProvider(pOut, lookupProvider, namespace, itemTagBuilders));
			gen.addProvider(new EngineGlobalLootModifierProvider(pOut, lookupProvider, namespace));

			// TODO: Fix client only stuff generating only on client for {@link ResourcePackMerger} to merge the correct client things too.
			if (FMLEnvironment.getDist().isClient()) {
				gen.addProvider(new EngineLanguageProvider(pOut, "en_us", namespace, blockBuilders, itemBuilders, soundBuilders));
				gen.addProvider(new EngineModelProvider(pOut, namespace, blockBuilders, itemBuilders));
				gen.addProvider(new EngineEquipmentAssetProvider(pOut));
				gen.addProvider(new EngineParticleDescriptionProvider(pOut));
				gen.addProvider(new EngineSoundDefinitionsProvider(pOut, namespace, soundBuilders));
			}

			gen.run();
		} catch (IOException e) {
			LOGGER.error("Failed to run data generator: {}", e.getMessage());
		}
	}

	private static void prepareOutputDirectories() {
		try {
			try {
				FileUtils.deleteDirectory(BundleDataGenerator.OUTPUT_ROOT.toFile());
			} catch (IOException e) {
				LOGGER.error("Could not clear Data Cache.");
			}
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
