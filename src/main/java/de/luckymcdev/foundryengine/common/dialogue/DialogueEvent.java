package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class DialogueEvent extends PlayerEvent {
	private final DialogueSession session;

	protected DialogueEvent(Player player, DialogueSession session) {
		super(player);
		this.session = session;
	}

	public DialogueSession getSession() {
		return session;
	}

	public static class Started extends DialogueEvent {
		public Started(Player player, DialogueSession session) {
			super(player, session);
		}
	}

	public static class Advanced extends DialogueEvent {
		private final String fromNodeId;
		private final String toNodeId;

		public Advanced(Player player, DialogueSession session, String fromNodeId, String toNodeId) {
			super(player, session);
			this.fromNodeId = fromNodeId;
			this.toNodeId = toNodeId;
		}

		public String getFromNodeId() {
			return fromNodeId;
		}

		public String getToNodeId() {
			return toNodeId;
		}
	}

	public static class OptionSelected extends DialogueEvent {
		private final DialogueOption option;

		public OptionSelected(Player player, DialogueSession session, DialogueOption option) {
			super(player, session);
			this.option = option;
		}

		public DialogueOption getOption() {
			return option;
		}
	}

	public static class Ended extends DialogueEvent {
		public Ended(Player player, DialogueSession session) {
			super(player, session);
		}
	}
}
