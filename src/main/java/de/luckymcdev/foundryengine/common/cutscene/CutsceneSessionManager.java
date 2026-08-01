package de.luckymcdev.foundryengine.common.cutscene;

import de.luckymcdev.foundryengine.common.cutscene.util.ServerScreenEffectManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side tracker for active cutscene playbacks per player (used for temporary invulnerability etc.).
 */
public class CutsceneSessionManager {
	private final Map<UUID, ArrayDeque<Integer>> playerInstances = new HashMap<>();

	public void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, ArrayDeque<Integer>>> it = playerInstances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, ArrayDeque<Integer>> entry = it.next();
			UUID uuid = entry.getKey();
			ArrayDeque<Integer> tickLengths = entry.getValue();

			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			if (player == null) {
				it.remove();
				continue;
			}

			if (tickLengths.isEmpty()) {
				it.remove();
				reassertInvulnerability(player);
				continue;
			}

			GameType mode = player.gameMode.getGameModeForPlayer();
			if (mode == GameType.SURVIVAL || mode == GameType.ADVENTURE) {
				player.setInvulnerable(true);
			}

			int head = tickLengths.removeFirst() - 1;
			if (head > 0) {
				tickLengths.addFirst(head);
			}
		}
	}

	public void cancelCutscene(ServerPlayer player) {
		ArrayDeque<Integer> q = playerInstances.get(player.getUUID());
		if (q == null || q.isEmpty()) {
			return;
		}
		q.removeFirst();
		if (q.isEmpty()) {
			playerInstances.remove(player.getUUID());
			reassertInvulnerability(player);
		}
	}

	/**
	 * Returns true if the player is still protected by either tracker (cutscene or screen
	 * effect). The predicate is evaluated from both trackers in one place so the outcome
	 * does not depend on which tracker ticks first.
	 */
	public boolean shouldPlayerBeProtected(ServerPlayer player) {
		return inCutscene(player) || ServerScreenEffectManager.inScreenEffect(player);
	}

	/**
	 * Re-asserts the player's invulnerability from the combined state of both trackers,
	 * clearing it only when no tracker still protects the player.
	 */
	private void reassertInvulnerability(ServerPlayer player) {
		GameType mode = player.gameMode.getGameModeForPlayer();
		if (mode == GameType.SURVIVAL || mode == GameType.ADVENTURE) {
			player.setInvulnerable(shouldPlayerBeProtected(player));
		}
	}

	/**
	 * Clears this tracker for a player leaving the server. The corresponding screen-effect
	 * tracker entry is cleared from the disconnect handler in the mod entrypoint so stale
	 * entries cannot leave the player permanently invulnerable.
	 */
	public void onPlayerDisconnect(ServerPlayer player) {
		playerInstances.remove(player.getUUID());
	}

	public boolean inCutscene(ServerPlayer player) {
		ArrayDeque<Integer> q = playerInstances.get(player.getUUID());
		return q != null && !q.isEmpty();
	}

	public void addInstance(ServerPlayer player, int ticks) {
		playerInstances.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>()).addLast(ticks);
	}
}
