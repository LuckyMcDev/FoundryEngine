package server.example

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode
import de.luckymcdev.foundryengine.common.dialogue.DialogueOption
import de.luckymcdev.foundryengine.common.dialogue.DialogueTree
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.resources.Identifier

class DialogueEntrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        registerSimpleNarrative()
        registerBranchingQuest()
        registerMerchantTree()
        registerMysteriousStranger()
        registerTutorial()
    }

    private void registerSimpleNarrative() {
        var id = Identifier.fromNamespaceAndPath("testbundle", "simple_story")
        var tree = new DialogueTree(id, "start")

        tree.addNode(new DialogueNode("start", "Narrator", "Welcome to the Foundry Engine dialogue system!\n\nThis is a simple narrative node with no choices — just click Next to continue.", 0x55FF55))
        tree.getNode("start").setNextNodeId("middle")

        tree.addNode(new DialogueNode("middle", "Narrator", "You can chain multiple narrative nodes together. Each one just shows text and waits for you to click Next.", 0x55FF55))
        tree.getNode("middle").setNextNodeId("end")

        tree.addNode(new DialogueNode("end", "Narrator", "The end! Type /engine dialogue next to go again, or /engine dialogue end to close.", 0x55FF55))

        Common.getDialogueManager().registerTree(tree)
    }

    private void registerBranchingQuest() {
        var id = Identifier.fromNamespaceAndPath("testbundle", "branching_quest")
        var tree = new DialogueTree(id, "village_gate")

        tree.addNode(new DialogueNode("village_gate", "Village Guard", "\u00A7e\"Halt, traveler! Before you enter, I must ask: what brings you to our village?\"", 0x55FFFF))
        tree.getNode("village_gate").getOptions().addAll([
            new DialogueOption("opt_1", "I seek adventure and treasure!", "guard_laughs"),
            new DialogueOption("opt_2", "Just passing through, friendly-like.", "guard_friendly"),
            new DialogueOption("opt_3", "\u00A7cNone of your business!", "guard_angry")
        ])

        tree.addNode(new DialogueNode("guard_laughs", "Village Guard", "\u00A7a\"Ha! An adventurer after my own heart. Head north — the old ruins have been stirring lately.\"\n\n\u00A77(You gained the quest: Visit the Northern Ruins)", 0x55FFFF))
        tree.getNode("guard_laughs").setNextNodeId("quest_started")

        tree.addNode(new DialogueNode("guard_friendly", "Village Guard", "\u00A7a\"Well met, friend. The village is open to you. Mind the merchant's stall — she doesn't take kindly to haggling.\"", 0x55FFFF))
        tree.getNode("guard_friendly").setNextNodeId("quest_started")

        tree.addNode(new DialogueNode("guard_angry", "Village Guard", "\u00A74\"Bold words for someone so close to the gate. I've got my eye on you, stranger.\"", 0x55FFFF))
        tree.getNode("guard_angry").setNextNodeId("quest_started")

        tree.addNode(new DialogueNode("quest_started", "Quest Log", "\u00A7bQuest started: Explore the Village\n\u00A77Speak to the villagers to learn more about this area.", 0x44AAFF))

        Common.getDialogueManager().registerTree(tree)
    }

    private void registerMerchantTree() {
        var id = Identifier.fromNamespaceAndPath("testbundle", "merchant")
        var tree = new DialogueTree(id, "greeting")

        tree.addNode(new DialogueNode("greeting", "Merchant Mabel", "Welcome to Mabel's Emporium! We've got the finest wares this side of the river.", 0xFFAA55))
        tree.getNode("greeting").getOptions().addAll([
            new DialogueOption("opt_1", "What are you selling?", "sell"),
            new DialogueOption("opt_2", "Tell me about the village.", "lore"),
            new DialogueOption("opt_3", "Goodbye!", "farewell")
        ])

        tree.addNode(new DialogueNode("sell", "Merchant Mabel", "Oh, a little bit of everything! Potions, enchanted trinkets, maps of the surrounding area... and I sometimes hear rumors from travelers.", 0xFFAA55))
        tree.getNode("sell").getOptions().addAll([
            new DialogueOption("opt_1", "Any rumors lately?", "rumor"),
            new DialogueOption("opt_2", "I'll just browse, thanks.", "farewell")
        ])

        tree.addNode(new DialogueNode("rumor", "Merchant Mabel", "\u00A7d\"They say a ghost has been spotted in the old windmill at night. The guard dismisses it, but old Bessie swears she saw lights flickering.\"\n\n\u00A77(Try /engine dialogue start testbundle:haunted_windmill screen)", 0xFFAA55))
        tree.getNode("rumor").getOptions().addAll([
            new DialogueOption("opt_1", "Fascinating! I'll check it out.", "farewell"),
            new DialogueOption("opt_2", "Sounds like a waste of time.", "farewell")
        ])

        tree.addNode(new DialogueNode("lore", "Merchant Mabel", "This village was founded over 200 years ago by a group of mages looking for a quiet place to study. The old tower at the edge of town still has traces of their magic, or so I'm told.", 0xFFAA55))
        tree.getNode("lore").setNextNodeId("greeting")

        tree.addNode(new DialogueNode("farewell", "Merchant Mabel", "Come back anytime, dearie! And watch your step on the road north.", 0xFFAA55))

        Common.getDialogueManager().registerTree(tree)
    }

    private void registerMysteriousStranger() {
        var id = Identifier.fromNamespaceAndPath("testbundle", "mysterious_stranger")
        var tree = new DialogueTree(id, "encounter")

        tree.addNode(new DialogueNode("encounter", "???", "{\"text\":\"A hooded figure emerges from the shadows...\",\"color\":\"dark_purple\",\"italic\":true}", 0xCC44FF))
        tree.getNode("encounter").getOptions().addAll([
            new DialogueOption("opt_1", "Who are you?", "reveal"),
            new DialogueOption("opt_2", "Leave me alone!", "leave")
        ])

        tree.addNode(new DialogueNode("reveal", "???", "{\"text\":\"\\\"I've been watching you, \",\"color\":\"dark_aqua\"}{\"text\":\"traveler\",\"color\":\"aqua\",\"bold\":true}{\"text\":\". You carry an energy I haven't seen in decades.\\\"\",\"color\":\"dark_aqua\"}", 0xCC44FF))
        tree.getNode("reveal").getOptions().addAll([
            new DialogueOption("opt_1", "What do you want?", "proposition"),
            new DialogueOption("opt_2", "I'm not interested.", "leave")
        ])

        tree.addNode(new DialogueNode("proposition", "???", "The stranger pulls back their hood, revealing ancient eyes. \"There's a \u00A7csealed vault\u00A7r beneath the village. I need someone who can \u00A76unlock\u00A7r it. The reward would be... \u00A7esignificant\u00A7r.\"", 0xCC44FF))
        tree.getNode("proposition").getOptions().addAll([
            new DialogueOption("opt_1", "I'll help you.", "accept"),
            new DialogueOption("opt_2", "Find someone else.", "leave")
        ])

        tree.addNode(new DialogueNode("accept", "Stranger", "\u00A7a\"Excellent. Meet me at midnight by the old well. Come prepared.\"\n\nThe stranger vanishes into the night.", 0xCC44FF))
        tree.getNode("accept").setNextNodeId("quest_active")

        tree.addNode(new DialogueNode("quest_active", "Quest Log", "\u00A7bQuest started: The Sealed Vault\n\u00A77Meet the stranger at midnight by the old well.", 0x44AAFF))

        tree.addNode(new DialogueNode("leave", "Stranger", "\u00A78\"Very well... but our paths will cross again.\"\n\nThe stranger melts back into the shadows.", 0x888888))

        Common.getDialogueManager().registerTree(tree)
    }

    private void registerTutorial() {
        var id = Identifier.fromNamespaceAndPath("testbundle", "tutorial")
        var tree = new DialogueTree(id, "intro")

        tree.addNode(new DialogueNode("intro", "System", "\u00A7b=== Dialogue Tutorial ===\u00A7r\n\n\u00A77This tutorial covers the basics of the dialogue system.\n\nCommands:\n\u00A7e/engine dialogue start <tree_id> screen\n\u00A7e/engine dialogue start <tree_id> chat\n\u00A7e/engine dialogue next\n\u00A7e/engine dialogue select <index>\n\u00A7e/engine dialogue end", 0x44AAFF))
        tree.getNode("intro").setNextNodeId("formatting")

        tree.addNode(new DialogueNode("formatting", "System", "\u00A7bText Formatting\u00A7r\n\nYou can use \u00A7cMinecraft \u00A7ecolor \u00A79codes\u00A7r in any text field.\n\n\u00A70Black \u00A71Dark Blue \u00A72Dark Green \u00A73Dark Aqua\n\u00A74Dark Red \u00A75Dark Purple \u00A76Gold \u00A77Gray\n\u00A78Dark Gray \u00A79Blue \u00A7aGreen \u00A7bAqua\n\u00A7cRed \u00A7dLight Purple \u00A7eYellow \u00A7fWhite\n\n\u00A7lBold \u00A7mStrikethrough \u00A7nUnderline \u00A7oItalic", 0x44AAFF))
        tree.getNode("formatting").setNextNodeId("json_components")

        tree.addNode(new DialogueNode("json_components", "System", "You can also use {\"text\":\"JSON\",\"color\":\"gold\",\"bold\":true} {\"text\":\"Component\",\"color\":\"light_purple\",\"italic\":true} {\"text\":\"syntax\",\"color\":\"green\"} for advanced formatting!\n\nExample: {\"text\":\"Click \",\"color\":\"gray\"}{\"text\":\"HERE\",\"color\":\"yellow\",\"bold\":true,\"underlined\":true}{\"text\":\" for details\",\"color\":\"gray\"}", 0x44AAFF))
        tree.getNode("json_components").setNextNodeId("conclusion")

        tree.addNode(new DialogueNode("conclusion", "System", "\u00A7aThat covers the basics!\u00A7r\n\nTry these trees:\n\u00A7e/engine dialogue start testbundle:simple_story screen\n\u00A7e/engine dialogue start testbundle:branching_quest screen\n\u00A7e/engine dialogue start testbundle:merchant screen\n\u00A7e/engine dialogue start testbundle:mysterious_stranger screen\n\nOr use \u00A7echat\u00A7r mode: replace \u00A7escreen\u00A7r with \u00A7echat\u00A7r", 0x44AAFF))

        Common.getDialogueManager().registerTree(tree)
    }

    @Override
    void onUnload() {
    }
}
