package de.luckymcdev.foundryengine.common.dialogue.display;

import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import net.minecraft.resources.Identifier;

/**
 * Client-side rendering contract for dialogue displays.
 * Implementations render dialogue in a screen overlay ({@link de.luckymcdev.foundryengine.client.dialogue.display.ScreenDialogueDisplay})
 * or via chat messages ({@link de.luckymcdev.foundryengine.client.dialogue.display.ChatDialogueDisplay}).
 */
public interface IDialogueDisplay {
	void showDialogue(Identifier treeId, DialogueSession session, DialogueNode node);

	void advanceDialogue(Identifier treeId, DialogueSession session, DialogueNode node);

	void endDialogue(Identifier treeId);

	boolean isActive();
}
