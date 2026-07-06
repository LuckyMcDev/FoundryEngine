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
				if (!ServerScreenEffectManager.inScreenEffect(player)) {
					player.setInvulnerable(false);
				}
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
			if (!ServerScreenEffectManager.inScreenEffect(player)) {
				player.setInvulnerable(false);
			}
		}
	}

	public boolean inCutscene(ServerPlayer player) {
		ArrayDeque<Integer> q = playerInstances.get(player.getUUID());
		return q != null && !q.isEmpty();
	}

	public void addInstance(ServerPlayer player, int ticks) {
		playerInstances.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>()).addLast(ticks);
	}
}

