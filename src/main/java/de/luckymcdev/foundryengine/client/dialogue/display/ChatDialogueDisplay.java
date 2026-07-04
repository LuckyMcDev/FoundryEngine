package de.luckymcdev.foundryengine.client.dialogue.display;

import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import de.luckymcdev.foundryengine.common.dialogue.display.IDialogueDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Chat-based dialogue display. Prints speaker text, options, and action hints
 * to the player's chat window. Requires manual input via {@code /engine dialogue next/select/end}.
 */
public class ChatDialogueDisplay implements IDialogueDisplay {

    private static final Component PREFIX = Component.translatable("foundryengine.dialogue.prefix");
    private boolean active;

    @Override
    public void showDialogue(Identifier treeId, DialogueSession session, DialogueNode node) {
        active = true;
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        player.sendSystemMessage(PREFIX.copy().append(
                Component.translatable("foundryengine.dialogue.speaker_format", node.getSpeaker(), node.getText())
        ));

        var options = node.getOptions();
        for (int i = 0; i < options.size(); i++) {
            var opt = options.get(i);
            player.sendSystemMessage(
                    Component.translatable("foundryengine.dialogue.option_format", i + 1, opt.getText())
            );
        }

        if (options.isEmpty()) {
            boolean hasNext = node.getNextNodeId() != null && !node.getNextNodeId().isBlank();
            if (hasNext) {
                player.sendSystemMessage(
                        Component.translatable("foundryengine.dialogue.hint_continue")
                );
            }
            player.sendSystemMessage(
                    Component.translatable("foundryengine.dialogue.hint_end")
            );
        } else {
            player.sendSystemMessage(
                    Component.translatable("foundryengine.dialogue.hint_select")
            );
        }
    }

    @Override
    public void advanceDialogue(Identifier treeId, DialogueSession session, DialogueNode node) {
        showDialogue(treeId, session, node);
    }

    @Override
    public void endDialogue(Identifier treeId) {
        active = false;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(
                    PREFIX.copy().append(Component.translatable("foundryengine.dialogue.ended"))
            );
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
