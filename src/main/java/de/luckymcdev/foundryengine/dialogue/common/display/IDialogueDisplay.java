package de.luckymcdev.foundryengine.dialogue.common.display;

import de.luckymcdev.foundryengine.dialogue.common.DialogueNode;
import de.luckymcdev.foundryengine.dialogue.common.DialogueSession;
import net.minecraft.resources.Identifier;

/**
 * Client-side rendering contract for dialogue displays.
 * Implementations render dialogue in a screen overlay ({@link de.luckymcdev.foundryengine.dialogue.client.display.ScreenDialogueDisplay})
 * or via chat messages ({@link de.luckymcdev.foundryengine.dialogue.client.display.ChatDialogueDisplay}).
 */
public interface IDialogueDisplay {
	void showDialogue(Identifier treeId, DialogueSession session, DialogueNode node);

	void advanceDialogue(Identifier treeId, DialogueSession session, DialogueNode node);

	void endDialogue(Identifier treeId);

	boolean isActive();
}
