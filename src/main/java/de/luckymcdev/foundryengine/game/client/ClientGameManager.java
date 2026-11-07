package de.luckymcdev.foundryengine.game.client;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.game.common.GameManager;
import de.luckymcdev.foundryengine.manager.api.ClientTickable;
import de.luckymcdev.foundryengine.manager.api.EngineManager;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ClientGameManager implements EngineManager, ClientTickable {
	private final GameManager gameManager;

	public ClientGameManager() {
		this.gameManager = Common.getGameManager();
	}

	@Override
	public void onClientTick(ClientTickEvent.Pre event) {
		Minecraft client = Minecraft.getInstance();
		gameManager.onClientTick(client, client.level);
	}
}
