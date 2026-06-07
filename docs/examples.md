# Examples

## Creating a Custom Item

```groovy
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.api.event.BundleEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Rarity

// A simple fire-resistant item
ItemBuilder.create(id("my_item"))
    .fireResistant()
    .stacksTo(16)
    .use { level, player, hand ->
        player.sendSystemMessage(Component.literal("Used!"))
        InteractionResult.SUCCESS
    }

// Register in onLoad
BundleEvents.registry {
    it.items(myItem)
}
```

## Creating a Block with Callbacks

```groovy
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.api.event.BundleEvents

BlockBuilder.create(id("magic_block"))
    .properties { it.strength(2.0f, 3.0f).lightLevel { 15 } }
    .stepOn { level, pos, onState, entity ->
        entity.setSecondsOnFire(3)
    }

BundleEvents.registry {
    it.blocks(magicBlock)
}
```

## Responding to an Event

```groovy
import de.luckymcdev.foundryengine.api.event.BlockEvents
import de.luckymcdev.foundryengine.api.event.PlayerEvents
import de.luckymcdev.foundryengine.api.event.ServerEvents

PlayerEvents.tick {
    if (it.player.tickCount % 20 == 0) {
        // Every second
    }
}

BlockEvents.broken {
    println "${it.player.name} broke a block at ${it.pos}"
}

ServerEvents.started {
    println "Server is ready!"
}
```

## Using Areas

```groovy
import de.luckymcdev.foundryengine.api.event.AreaEvents

AreaEvents.areaEnter {
    println "Player entered area: ${it.area.id()}"
}

AreaEvents.areaLeave {
    println "Player left area: ${it.area.id()}"
}
```

## Full Bundle Example

See the `ExampleBundles/testbundle` directory in the Foundry Engine repository for
a complete working bundle with items, blocks, recipes (all 9 types), sounds, commands,
and event listeners.
