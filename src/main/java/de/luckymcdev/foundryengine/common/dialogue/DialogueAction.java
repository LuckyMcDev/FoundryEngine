package de.luckymcdev.foundryengine.common.dialogue;

import net.minecraft.server.level.ServerPlayer;

/**
 * A named action executed during dialogue traversal (enter, option-select).
 * Registered via {@link DialogueManager#registerAction}.
 */
@FunctionalInterface
public interface DialogueAction {
    void execute(ServerPlayer player, DialogueSession session);
}
