package de.luckymcdev.foundryengine.game;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.debug.client.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.game.client.ClientGameManager;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;

@Mod(value = GameMod.MODID, dist = Dist.CLIENT)
public final class GameClientMod {
	public GameClientMod(IEventBus modBus, ModContainer modContainer) {
		Client.getManagers().register(ManagerKey.GAME_CLIENT, new ClientGameManager());
		modBus.addListener(this::onRegisterDebugEntry);
	}

	private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
		event.register(Common.id("gamestages_info"), new GameStagesDebugEntry());
	}
}
