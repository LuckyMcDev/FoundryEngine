package de.luckymcdev.foundryengine.game;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.event.common.GameEvents;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(GameMod.MODID)
public final class GameMod {
	public static final String MODID = "foundryengine_game";

	public GameMod(IEventBus modBus, ModContainer modContainer) {
		GameEvents.Internal.register(NeoForge.EVENT_BUS);
		Common.getGameStageHandler().register(modBus);
		var bus = NeoForge.EVENT_BUS;
		bus.register(Common.getGameStageHandler().blocks());
		bus.register(Common.getGameStageHandler().dimensions());
		bus.register(Common.getGameStageHandler().item());
		bus.register(Common.getGameStageHandler().loot());
		bus.register(Common.getGameStageHandler().mobs());
		bus.register(Common.getGameStageHandler().recipes());
		bus.addListener(this::onServerStarted);
		bus.addListener(this::onServerStopping);
		bus.addListener(this::onServerTick);
		bus.addListener(this::onLevelTick);
		bus.addListener(this::onPlayerChangedDimension);
	}

	private void onServerStarted(ServerStartedEvent event) {
		var server = event.getServer();
		String worldName = server.getWorldData().getLevelName();
		LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerAccess) server).getStorageSource();
		Common.getGameManager().setWorldDataPath(worldName,
			storageAccess.getLevelDirectory().path().resolve("foundryengine").resolve("game"));
		Common.getGameManager().autoStartAll(worldName);
		Common.getSavedDataManager().syncToAll();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getGameManager().stopAll();
		Common.getSavedDataManager().save();
	}

	private void onServerTick(ServerTickEvent.Post event) {
		var server = event.getServer();
		Common.getGameManager().beginServerTick();
		Common.getGameStageHandler().onPlayerTick(event);
		for (var level : server.getAllLevels()) {
			Common.getGameManager().tickServer(server, level);
		}
	}

	private void onLevelTick(LevelTickEvent.Post event) {
		Common.getGameManager().tickCommon(event.getLevel());
	}

	private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer) {
			Common.getSavedDataManager().syncToAll();
		}
	}
}
