package de.luckymcdev.foundryengine.game;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.debug.client.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.event.common.GameEvents;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(GameMod.MODID)
public final class GameMod implements EngineModPart {
	public static final String MODID = "foundryengine_game";

	public GameMod(IEventBus modBus, ModContainer modContainer) {
		this.wire(modBus);
		var b = bus();
		GameEvents.Internal.register(b);
		Common.getGameStageHandler().register(modBus);

		b.register(Common.getGameStageHandler().blocks());
		b.register(Common.getGameStageHandler().dimensions());
		b.register(Common.getGameStageHandler().item());
		b.register(Common.getGameStageHandler().loot());
		b.register(Common.getGameStageHandler().mobs());
		b.register(Common.getGameStageHandler().recipes());

		b.addListener(this::onServerStarted);
		b.addListener(this::onServerStopping);
		b.addListener(this::onServerTick);
		b.addListener(this::onLevelTick);
		b.addListener(this::onPlayerChangedDimension);

		if (FMLEnvironment.getDist().isClient()) {
			modBus.addListener(this::onRegisterDebugEntry);
			b.addListener(this::onClientTickPost);
		}
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getGameManager().stopAll();
		Common.getSavedDataManager().save();
	}

	private void onServerStarted(ServerStartedEvent event) {
		var server = event.getServer();
		String worldName = server.getWorldData().getLevelName();
		LevelStorageSource.LevelStorageAccess access =
			((MinecraftServerAccess) server).getStorageSource();
		Common.getGameManager().setWorldDataPath(worldName,
			access.getLevelDirectory().path().resolve("foundryengine").resolve("game"));
		Common.getGameManager().autoStartAll(worldName);
		Common.getSavedDataManager().syncToAll();
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

	private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
		event.register(Common.id("gamestages_info"), new GameStagesDebugEntry());
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		if (Minecraft.getInstance().level instanceof ClientLevel level) {
			Common.getGameManager().tickClient(Minecraft.getInstance(), level);
		}
	}
}