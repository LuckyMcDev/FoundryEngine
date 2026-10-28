package de.luckymcdev.foundryengine.dialogue.common;

import net.minecraft.server.level.ServerPlayer;

/**
 * A named predicate controlling whether a node or option is accessible.
 * Registered via {@link DialogueManager#registerCondition}.
 */
@FunctionalInterface
public interface DialogueCondition {
	boolean test(ServerPlayer player, DialogueSession session);
}
