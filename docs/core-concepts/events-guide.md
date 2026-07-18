# Events Guide

Events are how your bundle reacts to things happening in the game — a player joins, a block breaks, a mob dies, etc.

## How events work

FoundryEngine provides event classes with static methods. You pass a **callback** (a piece of code) that runs when the event happens:

```groovy
import de.luckymcdev.foundryengine.common.event.PlayerEvents

PlayerEvents.tick {
    def player = it.player
    if (player.isShiftKeyDown()) {
        println "${player.name} is sneaking!"
    }
}
```

In Groovy, `{ ... }` is a closure with an implicit `it` parameter containing the event data.

## What data does each event provide?

Different events provide different information:

```groovy
BlockEvents.broken {
    it.pos       // Where the block was
    it.player    // Who broke it
    it.state     // What the block was
}

PlayerEvents.tick {
    it.player    // The player
}

EntityEvents.death {
    it.entity    // What died
    it.source    // What killed it
}

ServerEvents.started { ->
    // No data — just a signal that the server started
}
```

## Registering content

The most important event is `BundleEvents.registry`. Use it to register items, blocks, sounds, and particles:

```groovy
BundleEvents.registry {
    it.items(myItem)
    it.blocks(myBlock)
    it.sounds(mySound)
    it.particles(myParticle)
}
```

## Listening to custom events

You can listen to any NeoForge event, even ones not wrapped by FoundryEngine:

```groovy
BundleEvents.custom(LivingDeathEvent.class, {
    println "${it.entity} died"
})
```

## Where to put events

Event listeners go inside the `onLoad()` method of your entrypoint:

```groovy
class Entrypoint implements BundleEntrypoint {
    @Override
    void onLoad() {
        PlayerEvents.tick {
            // runs every tick for every player
        }

        BundleEvents.registry {
            // register your content
        }
    }
}
```

## Next

- [Events Reference](events-reference.md) — complete list of all events
- [Creating Items](creating-items.md) — create content to register
