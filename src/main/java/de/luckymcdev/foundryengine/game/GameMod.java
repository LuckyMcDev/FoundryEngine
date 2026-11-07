package de.luckymcdev.foundryengine.game;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.config.ModuleConfig;
import de.luckymcdev.foundryengine.event.common.GameEvents;
import de.luckymcdev.foundryengine.game.common.GameManager;
import de.luckymcdev.foundryengine.game.common.stage.GameStageHandler;
import de.luckymcdev.foundryengine.game.common.stage.table.StageTableManager;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(GameMod.MODID)
public final class GameMod implements EngineModPart {
	public static final String MODID = "foundryengine_game";
	private static final ModuleConfig MODULE = new ModuleConfig("Game");

	public GameMod(IEventBus modBus, ModContainer modContainer) {
		if (!MODULE.registerAndCheck(modContainer)) {
			return;
		}
		this.wire(modBus);
		Common.getManagers().register(ManagerKey.GAME, new GameManager());
		Common.getManagers().register(ManagerKey.GAME_STAGE, new GameStageHandler());
		Common.getManagers().register(ManagerKey.STAGE_TABLE, new StageTableManager());
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
		b.addListener(this::onPlayerChangedDimension);

	}

	public static boolean isEnabled() {
		return MODULE.ENABLED.get();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getGameManager().stopAll();
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


	private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer) {
			Common.getSavedDataManager().syncToAll();
		}
	}

}