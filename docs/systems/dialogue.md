# Dialogue System

FoundryEngine includes a branching dialogue system for NPC conversations, quest dialogues, and interactive storytelling.

## How it works

A dialogue is a **tree of nodes**. Each node has:

- Speaker text (what the NPC says)
- Player options (what the player can respond)
- Conditions (show/hide options based on game state)
- Actions (side effects like giving items)

## Creating a dialogue tree

```groovy
import de.luckymcdev.foundryengine.common.dialogue.*

def tree = new DialogueTree(Common.id("my_dialogue"), "start")

// Root node — what the NPC says first
var startNode = new DialogueNode("start", "Villager", "Hello there!")
startNode.options.add(new DialogueOption("opt_greeting", "Greetings!", "welcome"))
tree.addNode(startNode)

// Second node
var welcomeNode = new DialogueNode("welcome", "Villager", "Nice to meet you!")
welcomeNode.options.add(new DialogueOption("opt_quest", "Got any work?", "quest"))
welcomeNode.options.add(new DialogueOption("opt_bye", "Goodbye", "end"))
tree.addNode(welcomeNode)

Common.getDialogueManager().registerTree(tree)
```

## Starting a dialogue

```groovy
// Screen mode (default) — full-screen overlay
Common.getDialogueManager().startDialogue(player, Common.id("my_dialogue"))

// Chat mode — prints to chat
Common.getDialogueManager().startDialogue(
        player, Common.id("my_dialogue"), DialogueDisplayMode.CHAT)
```

## Conditions and actions

Conditions control when an option is visible. Actions run effects.

```groovy
// Register a condition
Common.getDialogueManager().registerCondition("has_level", { player, session ->
    session.getVariable("level")?.toInteger() >= 5 ?: false
})

// Register an action
Common.getDialogueManager().registerAction("give_item", { player, session ->
    player.addItem(new ItemStack(Items.DIAMOND))
})

// Use them in a node
var questNode = new DialogueNode("quest", "Villager", "Bring me 5 diamonds!")
questNode.conditionIds.add("has_level")
questNode.enterActionIds.add("give_item")
```

## Dialogue events

```groovy
DialogueEvents.onStarted { event ->
    println "Dialogue started for ${event.player}"
}

DialogueEvents.onOptionSelected { event ->
    println "${event.player} selected ${event.optionId}"
}
```

## Dialogue sessions

Track per-player state during active dialogue:

```groovy
var session = Common.getDialogueManager().getSession(player)
if (session != null) {
    session.setVariable("level", "10")
}
```

## Next

- [Events Reference](../core-concepts/events-reference.md) — DialogueEvents
- [Commands](commands.md) — `/engine dialogue` command
- [Editor](editor.md) — Dialogue Editor panel
