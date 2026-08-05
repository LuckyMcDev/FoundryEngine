package de.luckymcdev.foundryengine.common.world.level.runtime;

import de.luckymcdev.foundryengine.common.world.level.EngineLevels;
import de.luckymcdev.foundryengine.interfaces.level.EngineDimensionOptions;
import de.luckymcdev.foundryengine.interfaces.registry.EngineRegistry;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class RuntimeLevelManager {
	private final MinecraftServer server;
	private final MinecraftServerAccess serverAccess;

	public RuntimeLevelManager(MinecraftServer server) {
		this.server = server;
		this.serverAccess = (MinecraftServerAccess) server;
	}

	private static MappedRegistry<LevelStem> getDimensionsRegistry(MinecraftServer server) {
		RegistryAccess registryManager = server.registries().compositeAccess();
		return (MappedRegistry<LevelStem>) registryManager.lookupOrThrow(Registries.LEVEL_STEM);
	}

	public RuntimeLevel add(ResourceKey<Level> levelKey, RuntimeLevelConfig config, RuntimeLevel.Style style) {
		LevelStem options = config.createDimensionOptions(this.server);

		if (style == RuntimeLevel.Style.TEMPORARY) {
			((EngineDimensionOptions) (Object) options).engine$setSave(false);
		}
		((EngineDimensionOptions) (Object) options).engine$setSaveProperties(false);

		MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
		try (var _ = EngineRegistry.thaw(dimensionsRegistry)) {
			var key = ResourceKey.create(Registries.LEVEL_STEM, levelKey.identifier());
			if (!dimensionsRegistry.containsKey(key)) {
				dimensionsRegistry.register(key, options, RegistrationInfo.BUILT_IN);
			}
		}

		RuntimeLevel level = config.getLevelConstructor().createLevel(this.server, levelKey, config, style);

		this.serverAccess.getLevels().put(level.dimension(), level);
		NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));

		// tick the level to ensure it is ready for use right away
		level.tick(() -> true);

		return level;
	}

	public void delete(ServerLevel level) {
		ResourceKey<Level> dimensionKey = level.dimension();

		if (this.serverAccess.getLevels().remove(dimensionKey, level)) {
			NeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));

			MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
			this.unregister((RuntimeLevel) level, dimensionKey, dimensionsRegistry, true);

			LevelStorageSource.LevelStorageAccess session = this.serverAccess.getStorageSource();
			File levelDirectory = session.getDimensionPath(dimensionKey).toFile();
			if (levelDirectory.exists()) {
				try {
					FileUtils.deleteDirectory(levelDirectory);
				} catch (IOException e) {
					EngineLevels.LOGGER.warn("Failed to delete level directory", e);
					try {
						FileUtils.forceDeleteOnExit(levelDirectory);
					} catch (IOException ignored) {
					}
				}
			}
		}
	}

	public void unload(ServerLevel level) {
		ResourceKey<Level> dimensionKey = level.dimension();

		if (this.serverAccess.getLevels().remove(dimensionKey, level)) {
			level.save(new ProgressListener() {
				@Override
				public void progressStartNoAbort(Component title) {
				}

				@Override
				public void progressStart(Component title) {
				}

				@Override
				public void progressStage(Component task) {
				}

				@Override
				public void progressStagePercentage(int percentage) {
				}

				@Override
				public void stop() {
				}
			}, true, false);

			NeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));

			MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(RuntimeLevelManager.this.server);
			this.unregister((RuntimeLevel) level, dimensionKey, dimensionsRegistry, false);
		}
	}

	private void unregister(RuntimeLevel level, ResourceKey<Level> dimensionKey, MappedRegistry<LevelStem> dimensionsRegistry, boolean alwaysDelete) {
		EngineRegistry.remove(dimensionsRegistry, dimensionKey.identifier());
	}
}
