# Events

FoundryEngine exposes an event system for reacting to nearly everything in the game. Events can be subscribed to from Groovy scripts or Java addons.

All API events are in package `de.luckymcdev.foundryengine.common.event` unless noted otherwise.

## Architecture

```
NeoForge Event Bus
    ↕ (listener forwards)
FoundryEngine EventGroupHolder
    ↕ (dispatches to)
    └── Java/Groovy callbacks (EventCallback<T>)
```

Each API event class contains:
- **Static `EventGroupHolder` fields** — one per event type, holding Java/Groovy callbacks
- **Static registration methods** — e.g. `BlockEvents.broken(callback)` adds your callback
- **Internal `Internal` class** — listens on NeoForge's bus and forwards to the `EventGroupHolder`

### EventCallback

A `@FunctionalInterface` with a single method: `void execute(T event)`. In Groovy this can be a closure:

```groovy
BlockEvents.broken {
    println it.pos
}
```

### EventGroupHolder

Wraps an `EventGroup` (thread-safe list of callbacks). When an event fires it calls all registered Java/Groovy `EventCallback` listeners sequentially.

### What Data Does Each Event Expose?

In Groovy, the closure parameter `it` provides different properties depending on the event type. Here's a quick reference:

```groovy
BlockEvents.broken { it -> it.pos }     // BlockPos, player, level, state
ItemEvents.pickedUp { it -> it.player }  // player, stack, level
EntityEvents.death { it -> it.entity }   // entity, source
PlayerEvents.tick { it -> it.player }    // player
ServerEvents.started { /* no data */ }   // signal only
```

For every event class listed below, the `it` object in your closure corresponds to the **wrapped event type**. For standard NeoForge events wrapped by the API, `it` provides all the getters of that event class. Use your IDE's auto-complete or check the NeoForge documentation for the full list of available properties.

### Custom events

Listen for any NeoForge event class, even ones FoundryEngine does not explicitly wrap:

```groovy
BundleEvents.custom(LivingDeathEvent.class, {
    println "${it.entity} died"
})
```

---

## AreaEvents

Package: `de.luckymcdev.foundryengine.common.event.AreaEvents`

| Event | Description |
|---|---|
| `register(cb)` | Called for every `ServerLevel` as it loads |

Fires when server levels load, providing a hook to create areas. Area runtime behavior uses module interfaces (`AreaTickModule`, `AreaEnterModule`, `AreaLeaveModule`, `AreaBlockModule`, `AreaRenderModule`).

---

## BlockEvents

Package: `de.luckymcdev.foundryengine.common.event.BlockEvents`

| Event | Description |
|---|---|
| `broken(cb)` | A block is broken |
| `placed(cb)` | A block is placed |
| `neighborNotify(cb)` | Block neighbor update |
| `leftClicked(cb)` | Player left-clicks a block |
| `rightClicked(cb)` | Player right-clicks a block |
| `farmlandTrampled(cb)` | Farmland is trampled |
| `modification(cb)` | Modify block behavior post-registration |

> **⚠️ BlockModificationEvent is unstable.** The modification API has known issues and may not produce the expected results in all cases. Use with caution.

```groovy
BlockEvents.modification { it -> it.hasCollision(false) }
```

The `it` parameter exposes: `hasCollision`, `explosionResistance`, `lightEmission`, `soundType`, `friction`, `speedFactor`, `jumpFactor`, `randomlyTicking`, `setDestroySpeed`, `setRequiresTool`.

---

## BundleEvents

Package: `de.luckymcdev.foundryengine.common.event.BundleEvents`

Core lifecycle events. **The most important event class for bundle developers.**

| Event | Description |
|---|---|
| `registry(cb)` | Register items, blocks, sounds, particles |
| `vanillaGame(cb)` | Vanilla game events (advancement, etc.) |
| `commonSetup(cb)` | FML common setup phase |
| `clientSetup(cb)` | FML client-only setup |
| `dedicatedServerSetup(cb)` | Dedicated server setup |
| `postInit(cb)` | After all mods initialize |
| `serverAboutToStart(cb)` | Server about to start |
| `dataGen(cb)` | Bundle data generation |

```groovy
BundleEvents.registry {
    it.items(myItemBuilder)
    it.blocks(myBlockBuilder)
    it.particles(myParticleBuilder)
    it.sounds(mySoundBuilder)
}

BundleEvents.dataGen {
    it.addProvider(myDataProvider)
}
```

### BundleEvents.custom

Listen for any NeoForge `Event` subclass, even ones not wrapped by the API:

```groovy
BundleEvents.custom(LivingDeathEvent.class, {
    println "${it.entity} died"
})
```

---

## ClientEvents

Package: `de.luckymcdev.foundryengine.common.event.ClientEvents`

Only fires on the **client side**.

| Event | Description |
|---|---|
| `tick(cb)` | End of client tick |
| `stopped(cb)` | Client fully stopped |
| `stopping(cb)` | Client is stopping |
| `chat(cb)` | Client sending a chat message |
| `keyMappings(cb)` | Register key mappings |
| `renderGui(cb)` | After GUI is rendered |
| `renderGuiLayer(cb)` | After a GUI layer is rendered |
| `renderHand(cb)` | Hand rendering |
| `renderAfterLevel(cb)` | After level rendering |
| `loggedIn(cb)` | Client logged into a server |
| `loggedOut(cb)` | Client logged out |

---

## CommandEvents

Package: `de.luckymcdev.foundryengine.common.event.CommandEvents`

| Event | Description |
|---|---|
| `register(cb)` | Register server commands |
| `registerClient(cb)` | Register client-side commands |

```groovy
CommandEvents.register {
    def dispatcher = it.dispatcher
    dispatcher.register(Commands.literal("mycommand")
        .executes(ctx -> {
            println "My command executed!"
            return 1
        })
    )
}
```

---

## EntityEvents

Package: `de.luckymcdev.foundryengine.common.event.EntityEvents`

| Event | Description |
|---|---|
| `joinLevel(cb)` | Entity enters a level |
| `death(cb)` | Living entity dies |
| `drops(cb)` | Entity drops items on death |
| `hurt(cb)` | Entity takes damage (post) |
| `spawned(cb)` | Entity spawned into world |
| `checkSpawn(cb)` | Check entity spawn conditions |

---

## GameEvents

Package: `de.luckymcdev.foundryengine.common.event.GameEvents`

Game session lifecycle events.

| Event | Description |
|---|---|
| `onStarting(cb)` | Game session starting |
| `onStarted(cb)` | Game session started |
| `onStopping(cb)` | Game session stopping |
| `onStopped(cb)` | Game session stopped |

---

## ItemEvents

Package: `de.luckymcdev.foundryengine.common.event.ItemEvents`

| Event | Description |
|---|---|
| `pickedUp(cb)` | Item picked up by player |
| `destroyed(cb)` | Item is destroyed (used up) |
| `rightClicked(cb)` | Player right-clicks with item |
| `crafted(cb)` | Item crafted |
| `dropped(cb)` | Item thrown/dropped |
| `foodEaten(cb)` | Food consumption finished |
| `smelted(cb)` | Item smelted in furnace |
| `dynamicTooltips(cb)` | Item tooltip rendering |
| `entityInteracted(cb)` | Player interacts entity with item |
| `firstLeftClicked(cb)` | Left-click on empty space |
| `firstRightClicked(cb)` | Right-click on empty space |
| `modification(cb)` | Modify item behavior post-registration |

> **⚠️ ItemModificationEvent is unstable.** The modification API has known issues and may not produce the expected results in all cases. Use with caution.

```groovy
ItemEvents.modification { it -> it.setMaxStackSize(64) }
```

The `it` parameter exposes: `setMaxStackSize`, `setMaxDamage`, `setUnbreakable`, `setFood`, `setTool`, `setAttributeModifiers`.

---

## LevelEvents

Package: `de.luckymcdev.foundryengine.common.event.LevelEvents`

| Event | Description |
|---|---|
| `load(cb)` | Level/dimension is loaded |
| `unload(cb)` | Level/dimension is unloaded |
| `save(cb)` | Level data is saved |
| `tick(cb)` | End of level tick |
| `beforeExplosion(cb)` | Before an explosion detonates |
| `afterExplosion(cb)` | After an explosion detonates |

---

## NetworkEvents

Package: `de.luckymcdev.foundryengine.common.event.NetworkEvents`

| Event                      | Description                            |
|----------------------------|----------------------------------------|
| `login(cb)`                | Player network login                   |
| `logout(cb)`               | Player network logout                  |
| `onCustomDataReceived(cb)` | CustomDataPacket received (both sides) |

---

## PlayerEvents

Package: `de.luckymcdev.foundryengine.common.event.PlayerEvents`

| Event | Description |
|---|---|
| `loggedIn(cb)` | Player logged in |
| `loggedOut(cb)` | Player logged out |
| `tick(cb)` | End of player tick |
| `chat(cb)` | Player sends chat message |
| `advancement(cb)` | Player earns advancement |
| `chestClosed(cb)` | Player closes a container |
| `chestOpened(cb)` | Player opens a container |
| `respawned(cb)` | Player respawns |
| `decorateChat(cb)` | Decorate received chat messages |

---

## RecipeEvents

Package: `de.luckymcdev.foundryengine.common.event.RecipeEvents`

| Event | Description |
|---|---|
| `recipesReceived(cb)` | Client received recipe list |
| `modifyRecipes(cb)` | Modify recipe JSONs at reload |

---

## ServerEvents

Package: `de.luckymcdev.foundryengine.common.event.ServerEvents`

| Event | Description |
|---|---|
| `aboutToStart(cb)` | Server about to start |
| `started(cb)` | Server has started |
| `starting(cb)` | Server is starting |
| `stopped(cb)` | Server has stopped |
| `stopping(cb)` | Server is stopping |
| `tick(cb)` | End of server tick |
| `tags(cb)` | Game tags were updated/reloaded |

---

## SlotEvents

Package: `de.luckymcdev.foundryengine.common.event.SlotEvents`

| Event | Description |
|---|---|
| `modification(cb)` | Container menu opened or screen initialized |

Fired with `AbstractContainerMenu` when a container is opened or a screen initializes.

---

## StageEvents

Package: `de.luckymcdev.foundryengine.common.event.StageEvents`

Game stage progression events.

| Event | Description |
|---|---|
| `adding(cb)` | Before a stage is added (cancellable) |
| `removing(cb)` | Before a stage is removed (cancellable) |
| `added(cb)` | After a stage has been added |
| `removed(cb)` | After a stage has been removed |

---

## DialogueEvents

Package: `de.luckymcdev.foundryengine.common.event.DialogueEvents`

Dialogue tree traversal events.

| Event | Description |
|---|---|
| `onStarted(cb)` | Dialogue started for a player |
| `onAdvanced(cb)` | Dialogue advanced to next node |
| `onOptionSelected(cb)` | Player selected an option |
| `onEnded(cb)` | Dialogue ended |

---

## Using events in Groovy scripts

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.event.*

class Entrypoint implements BundleEntrypoint {
    @Override
    void onLoad() {
        PlayerEvents.tick {
            def player = it.player
            if (player.isShiftKeyDown()) {
                println "${player.name} is sneaking!"
            }
        }

        BlockEvents.broken {
            println it.pos
        }

        BundleEvents.registry {
            it.items(myItemBuilder)
        }
    }

    @Override
    void onUnload() {}
}
```

## Internal and addon events

These events are available for Java addon developers using NeoForge's event bus directly.

### TitleScreenModifyEvent

Package: `de.luckymcdev.foundryengine.common.event.modification.TitleScreenModifyEvent`

`ICancellableEvent` -- Fired when title screen buttons are being created. Can cancel `SINGLEPLAYER`, `MULTIPLAYER`, or `REALMS` buttons.

```java
@SubscribeEvent
public void onTitleScreen(TitleScreenModifyEvent event) {
    if (event.getButtonType() == TitleScreenModifyEvent.ButtonType.REALMS) {
        event.setCanceled(true);
    }
}
```

### GameStageEvent

Package: `de.luckymcdev.foundryengine.common.game.stage.GameStageEvent`

Abstract event — listen to subclasses:

| Subclass | Cancellable | Description |
|---|---|---|
| `GameStageEvent.Add` | Yes | Before a stage is added to a player |
| `GameStageEvent.Remove` | Yes | Before a stage is removed from a player |
| `GameStageEvent.Added` | No | After a stage has been added |
| `GameStageEvent.Removed` | No | After a stage has been removed |

All four expose `getStageName()` and `getPlayer()`.

### BlockModificationEvent

Package: `de.luckymcdev.foundryengine.common.event.modification.BlockModificationEvent`

Builder-pattern modification of block properties post-registration. See [BlockEvents](#blockevents).

### ItemModificationEvent

Package: `de.luckymcdev.foundryengine.common.event.modification.ItemModificationEvent`

Builder-pattern modification of item properties post-registration. See [ItemEvents](#itemevents).

## See also

- [Builders](builders) -- Creating content that can be registered
