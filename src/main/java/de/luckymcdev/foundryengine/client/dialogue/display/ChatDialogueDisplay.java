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

    private static final Component PREFIX = Component.literal("[§bDialogue§r] ");
    private boolean active;

    @Override
    public void showDialogue(Identifier treeId, DialogueSession session, DialogueNode node) {
        active = true;
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        player.sendSystemMessage(PREFIX.copy().append(
                Component.literal("§e<" + node.getSpeaker() + ">§r " + node.getText())
        ));

        var options = node.getOptions();
        for (int i = 0; i < options.size(); i++) {
            var opt = options.get(i);
            player.sendSystemMessage(
                    Component.literal("§7[" + (i + 1) + "]§r " + opt.getText())
            );
        }

        if (options.isEmpty()) {
            boolean hasNext = node.getNextNodeId() != null && !node.getNextNodeId().isBlank();
            if (hasNext) {
                player.sendSystemMessage(
                        Component.literal("§7[Type §o/engine dialogue next§r§7 to continue]§r")
                );
            }
            player.sendSystemMessage(
                    Component.literal("§7[Type §o/engine dialogue end§r§7 to close]§r")
            );
        } else {
            player.sendSystemMessage(
                    Component.literal("§7[Type §o/engine dialogue select <number>§r§7 to respond]§r")
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
                    PREFIX.copy().append(Component.literal("§7Dialogue ended."))
            );
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
