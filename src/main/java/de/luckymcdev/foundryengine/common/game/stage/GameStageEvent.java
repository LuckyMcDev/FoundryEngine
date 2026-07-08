package de.luckymcdev.foundryengine.common.game.stage;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class GameStageEvent extends PlayerEvent {
	private final Identifier stage;

	protected GameStageEvent(Player player, Identifier stage) {
		super(player);
		this.stage = stage;
	}

	public Identifier getStage() {
		return this.stage;
	}

	public static class Add extends GameStageEvent implements ICancellableEvent {
		public Add(Player player, Identifier stage) {
			super(player, stage);
		}
	}

	public static class Remove extends GameStageEvent implements ICancellableEvent {
		public Remove(Player player, Identifier stage) {
			super(player, stage);
		}
	}

	public static class Added extends GameStageEvent {
		public Added(Player player, Identifier stage) {
			super(player, stage);
		}
	}

	public static class Removed extends GameStageEvent {
		public Removed(Player player, Identifier stage) {
			super(player, stage);
		}
	}
}
