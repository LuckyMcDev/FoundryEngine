package showcase

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.dialogue.*
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class DialogueEntrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path)
    }

    @Override
    void onLoad() {
        def manager = Common.getDialogueManager()

        def tree = new DialogueTree(id("welcome_guide"), "start")

        def startNode = new DialogueNode("start", "Guide",
            "Welcome, adventurer! I can teach you about this world.")
        def explainItemsNode = new DialogueNode("explain_items", "Guide",
            "You can craft special items like magic gems and wands." +
            " Try gathering some diamonds and emeralds!")
        def explainAreasNode = new DialogueNode("explain_areas", "Guide",
            "I have marked some special areas on your map." +
            " Seek out the Healing Zone for safe passage.")
        def farewellNode = new DialogueNode("farewell", "Guide",
            "Good luck on your journey! May the sparkle guide you.")

        startNode.getOptions().add(new DialogueOption("opt_items", "Tell me about items", "explain_items"))
        startNode.getOptions().add(new DialogueOption("opt_areas", "What are those colored zones?", "explain_areas"))
        startNode.getOptions().add(new DialogueOption("opt_leave", "Goodbye!", "farewell"))

        explainItemsNode.getOptions().add(new DialogueOption("opt_areas_from_items", "And the zones?", "explain_areas"))
        explainItemsNode.getOptions().add(new DialogueOption("opt_bye_from_items", "Thanks, bye!", "farewell"))

        explainAreasNode.getOptions().add(new DialogueOption("opt_bye_from_areas", "Thanks!", "farewell"))

        manager.registerAction("give_starting_kit", { player, session ->
            player.sendSystemMessage(
                Component.literal("§aYou received a starting kit!"))
        } as DialogueAction)
        farewellNode.getEnterActionIds().add("give_starting_kit")

        tree.addNode(startNode)
        tree.addNode(explainItemsNode)
        tree.addNode(explainAreasNode)
        tree.addNode(farewellNode)
        tree.setStyle(new DialogueStyle())

        manager.registerTree(tree)
    }
}
