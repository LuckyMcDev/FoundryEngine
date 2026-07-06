package de.luckymcdev.foundryengine.client.dialogue;

import de.luckymcdev.foundryengine.client.dialogue.display.ChatDialogueDisplay;
import de.luckymcdev.foundryengine.client.dialogue.display.ScreenDialogueDisplay;
import de.luckymcdev.foundryengine.common.dialogue.DialogueDisplayMode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import de.luckymcdev.foundryengine.common.dialogue.DialogueTree;
import de.luckymcdev.foundryengine.common.dialogue.display.IDialogueDisplay;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side counterpart to {@link de.luckymcdev.foundryengine.common.dialogue.DialogueManager}.
 * Routes incoming dialogue packets to the correct display implementation
 * based on the session's {@link DialogueDisplayMode}.
 */
public class ClientDialogueManager {
	private final Map<Identifier, DialogueTree> trees = new HashMap<>();
	private final ScreenDialogueDisplay screenDisplay = new ScreenDialogueDisplay();
	private final ChatDialogueDisplay chatDisplay = new ChatDialogueDisplay();
	private IDialogueDisplay activeDisplay;
	private Identifier activeTreeId;
	private DialogueSession activeSession;
	private boolean inDialogue;

	private IDialogueDisplay displayFor(DialogueSession session) {
		return session.getDisplayMode() == DialogueDisplayMode.CHAT ? chatDisplay : screenDisplay;
	}

	public boolean isInDialogue() {
		return inDialogue;
	}

	public DialogueSession getActiveSession() {
		return activeSession;
	}

	public Identifier getActiveTreeId() {
		return activeTreeId;
	}

	public void startDialogue(Identifier treeId, DialogueSession session, DialogueNode node) {
		this.activeTreeId = treeId;
		this.activeSession = session;
		this.inDialogue = true;
		this.activeDisplay = displayFor(session);
		activeDisplay.showDialogue(treeId, session, node);
	}

	public void advanceDialogue(DialogueSession session, DialogueNode node) {
		this.activeSession = session;
		var display = displayFor(session);
		if (display != activeDisplay) {
			activeDisplay.endDialogue(activeTreeId);
			activeDisplay = display;
			activeDisplay.showDialogue(activeTreeId, session, node);
		} else {
			activeDisplay.advanceDialogue(activeTreeId, session, node);
		}
	}

	public void endDialogue() {
		this.inDialogue = false;
		if (activeDisplay != null) {
			activeDisplay.endDialogue(activeTreeId);
		}
		this.activeDisplay = null;
		this.activeSession = null;
		this.activeTreeId = null;
	}

	public void syncTrees(Map<Identifier, DialogueTree> trees) {
		this.trees.clear();
		this.trees.putAll(trees);
	}
}
