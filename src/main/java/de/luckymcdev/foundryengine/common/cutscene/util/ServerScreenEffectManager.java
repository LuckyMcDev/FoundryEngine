package de.luckymcdev.foundryengine.common.cutscene.util;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerScreenEffectManager {
	private static final HashMap<ServerPlayer, ArrayList<Integer>> playerTracker = new HashMap<>();

	public static void tick() {
		Iterator<Map.Entry<ServerPlayer, ArrayList<Integer>>> it = playerTracker.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<ServerPlayer, ArrayList<Integer>> entry = it.next();
			ServerPlayer player = entry.getKey();
			ArrayList<Integer> tickLengths = entry.getValue();

			if (tickLengths.isEmpty()) {
				it.remove();
				reassertInvulnerability(player);
				continue;
			}

			GameType mode = player.gameMode.getGameModeForPlayer();
			if (mode == GameType.SURVIVAL || mode == GameType.ADVENTURE) {
				player.setInvulnerable(true);
			}

			tickLengths.set(0, tickLengths.getFirst() - 1);
			if (tickLengths.getFirst() <= 0) {
				tickLengths.removeFirst();
			}
		}
	}

	public static boolean inScreenEffect(ServerPlayer player) {
		return playerTracker.get(player) != null;
	}

	/**
	 * Returns true if the player is still protected by either tracker (screen effect or
	 * cutscene). The predicate is evaluated from both trackers in one place so the outcome
	 * does not depend on which tracker ticks first.
	 */
	public static boolean shouldPlayerBeProtected(ServerPlayer player) {
		return inScreenEffect(player) || Common.getCutsceneSessionManager().inCutscene(player);
	}

	/**
	 * Re-asserts the player's invulnerability from the combined state of both trackers,
	 * clearing it only when no tracker still protects the player.
	 */
	private static void reassertInvulnerability(ServerPlayer player) {
		GameType mode = player.gameMode.getGameModeForPlayer();
		if (mode == GameType.SURVIVAL || mode == GameType.ADVENTURE) {
			player.setInvulnerable(shouldPlayerBeProtected(player));
		}
	}

	/**
	 * Clears this tracker for a player leaving the server.
	 */
	public static void onPlayerDisconnect(ServerPlayer player) {
		playerTracker.remove(player);
	}

	public static void addInstance(ServerPlayer player, int ticks) {
		ArrayList<Integer> playerInstances = playerTracker.get(player);
		if (playerInstances != null) {
			playerInstances.add(ticks);
			return;
		}
		playerInstances = new ArrayList<>();
		playerInstances.add(ticks);
		playerTracker.put(player, playerInstances);
	}
}
