# Dialogue system

FoundryEngine includes a branching dialogue system for creating NPC conversations, quest dialogues, and interactive storytelling. Dialogues are defined as trees of nodes with speaker text, player options, conditions, and actions.

## Architecture

```
DialogueTree (named collection of nodes)
    ├── DialogueNode (speaker text + options)
    │   ├── DialogueOption (player choice → target node)
    │   ├── DialogueCondition (predicate to show/hide)
    │   └── DialogueAction (side effect on enter/select)
    └── DialogueStyle (visual styling for the UI)

DialogueManager (server-side)
    ├── registerTree(), getTree(), replaceAll()
    ├── startDialogue(player, treeId)
    ├── advance(player) / selectOption(player, optionId)
    └── endDialogue(player)

ClientDialogueManager (client-side)
    ├── SCREEN display (full-screen overlay widget)
    └── CHAT display (printed to chat window)
```

## Dialogue Tree

A dialogue tree is a collection of nodes with one designated root node. Trees are identified by a unique `Identifier` and persisted per dimension via `DialogueSavedData`.

```groovy
import de.luckymcdev.foundryengine.common.dialogue.*

var tree = new DialogueTree(Common.id("my_dialogue"), "start")

var startNode = new DialogueNode("start", "Villager", "Hello there!")
startNode.options.add(new DialogueOption("opt_greeting", "Greetings!", "welcome"))
tree.addNode(startNode)

var welcomeNode = new DialogueNode("welcome", "Villager", "Nice to meet you!")
welcomeNode.options.add(new DialogueOption("opt_quest", "Got any work?", "quest"))
welcomeNode.options.add(new DialogueOption("opt_bye", "Goodbye", "end"))
tree.addNode(welcomeNode)

Common.getDialogueManager().registerTree(tree)
```

## Display Modes

| Mode       | Enum                         | Description                                                                   |
|------------|------------------------------|-------------------------------------------------------------------------------|
| **Screen** | `DialogueDisplayMode.SCREEN` | Full-screen overlay with styled panel, speaker name, text, and option buttons |
| **Chat**   | `DialogueDisplayMode.CHAT`   | Dialogue printed to the chat window                                           |

## Starting a Dialogue

```groovy
def player = ... // ServerPlayer

// Default screen mode
Common.getDialogueManager().startDialogue(player, Common.id("my_dialogue"))

// Chat mode
Common.getDialogueManager().startDialogue(player, Common.id("my_dialogue"), DialogueDisplayMode.CHAT)
```

## Conditions and Actions

Conditions gate whether a node or option is visible. Actions execute side effects during dialogue traversal.

```groovy
// Register a condition
Common.getDialogueManager().registerCondition("has_level", { player, session ->
    session.getVariable("level")?.toInteger() >= 5 ?: false
})

// Register an action
Common.getDialogueManager().registerAction("give_item", { player, session ->
    player.addItem(new ItemStack(Items.DIAMOND))
})

// Use in a node
var questNode = new DialogueNode("quest", "Villager", "Bring me 5 diamonds!")
questNode.conditionIds.add("has_level")
questNode.enterActionIds.add("give_item")
```

## Dialogue Events

| Event                                 | Description                    |
|---------------------------------------|--------------------------------|
| `DialogueEvents.onStarted(cb)`        | Dialogue started for a player  |
| `DialogueEvents.onAdvanced(cb)`       | Dialogue advanced to next node |
| `DialogueEvents.onOptionSelected(cb)` | Player selected an option      |
| `DialogueEvents.onEnded(cb)`          | Dialogue ended                 |

```groovy
import de.luckymcdev.foundryengine.common.event.DialogueEvents

DialogueEvents.onStarted {
    println "Dialogue started for ${it.player}"
}

DialogueEvents.onOptionSelected {
    println "${it.player} selected option ${it.optionId}"
}
```

## Dialogue Session

The `DialogueSession` tracks per-player state during an active dialogue: current node, history, and custom variables.

```groovy
var session = Common.getDialogueManager().getSession(player)
if (session != null) {
    session.setVariable("level", "10")
    println "History: ${session.history}"
}
```

## See also

- [Events](../core/events) -- DialogueEvents reference
- [Commands](commands) -- `/engine dialogue` command
- [In-Game Editor](editor) -- Dialogue Editor panel for visual editing
