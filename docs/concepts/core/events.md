# Events

FoundryEngine exposes a rich event system for reacting to nearly everything in the game. Events can be subscribed to from Groovy scripts, Blueprints, or Java addons.

All API events are in package `de.luckymcdev.foundryengine.api.event` unless noted otherwise.

## Architecture

```
NeoForge Event Bus
    ↕ (listener forwards)
FoundryEngine EventGroupHolder
    ↕ (dispatches to)
    ├── Java/Groovy callbacks (EventCallback<T>)
    └── Blueprint visual scripting nodes
```

Each API event class contains:
- **Static `EventGroupHolder` fields** — one per event type, holding both Java callbacks and Blueprint node links
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

Wraps an `EventGroup` (thread-safe list of callbacks) plus an optional Blueprint node ID and context mapper. When an event fires it:
1. Calls all registered Java/Groovy `EventCallback` listeners sequentially
2. If a Blueprint node ID is set, maps the event to a `Map<String, Object>` context and executes matching Blueprint graphs

### What Data Does Each Event Expose?

In Groovy, the closure parameter `it` provides different properties depending on the event type. Here's a quick reference:

```groovy
BlockEvents.broken { it -> it.pos }     // BlockPos, player, level, state
ItemEvents.pickedUp { it -> it.player }  // player, stack, level
EntityEvents.death { it -> it.entity }   // entity, source
PlayerEvents.tick { it -> it.player }    // player
ServerEvents.started { /* no data */ }   // just a signal
```

For every event class listed below, the `it` object in your closure corresponds to the **wrapped event type**. For standard NeoForge events wrapped by the API, `it` provides all the getters of that event class. Use your IDE's auto-complete or check the NeoForge documentation for the full list of available properties.

### Custom Events

Listen for any NeoForge event class, even those FoundryEngine does not explicitly wrap:

```groovy
BundleEvents.custom(LivingDeathEvent.class, {
    println "${it.entity} died"
})
```

---

## AreaEvents

Package: `de.luckymcdev.foundryengine.common.event.AreaEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `register(cb)` | Called for every `ServerLevel` as it loads | — |

Fires when server levels load, providing a hook to create areas. Area runtime behavior uses module interfaces (`AreaTickModule`, `AreaEnterModule`, `AreaLeaveModule`, `AreaBlockModule`, `AreaRenderModule`).

---

## BlockEvents

Package: `de.luckymcdev.foundryengine.api.event.BlockEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `broken(cb)` | A block is broken | `EVENT_BLOCK_BROKEN` |
| `placed(cb)` | A block is placed | `EVENT_BLOCK_PLACED` |
| `neighborNotify(cb)` | Block neighbor update | — |
| `leftClicked(cb)` | Player left-clicks a block | `EVENT_BLOCK_LEFT_CLICKED` |
| `rightClicked(cb)` | Player right-clicks a block | `EVENT_BLOCK_RIGHT_CLICKED` |
| `farmlandTrampled(cb)` | Farmland is trampled | `EVENT_FARMLAND_TRAMPLED` |
| `modification(cb)` | Modify block behavior post-registration | — |

> **⚠️ BlockModificationEvent is unstable.** The modification API has known issues and may not produce the expected results in all cases. Use with caution.

```groovy
BlockEvents.modification { it -> it.hasCollision(false) }
```

The `it` parameter exposes: `hasCollision`, `explosionResistance`, `lightEmission`, `soundType`, `friction`, `speedFactor`, `jumpFactor`, `randomlyTicking`, `setDestroySpeed`, `setRequiresTool`.

---

## BundleEvents

Package: `de.luckymcdev.foundryengine.api.event.BundleEvents`

Core lifecycle events. **The most important event class for bundle developers.**

| Event | Description | Blueprint Node |
|---|---|---|
| `registry(cb)` | Register items, blocks, recipes, sounds, particles | `EVENT_REGISTRY` |
| `vanillaGame(cb)` | Vanilla game events (advancement, etc.) | `EVENT_VANILLA_GAME` |
| `commonSetup(cb)` | FML common setup phase | `EVENT_COMMON_SETUP` |
| `clientSetup(cb)` | FML client-only setup | `EVENT_CLIENT_SETUP` |
| `dedicatedServerSetup(cb)` | Dedicated server setup | `EVENT_DEDICATED_SERVER_SETUP` |
| `postInit(cb)` | After all mods initialize | `EVENT_POST_INIT` |
| `serverAboutToStart(cb)` | Server about to start | `EVENT_SERVER_ABOUT_TO_START` |
| `dataGen(cb)` | Bundle data generation | `EVENT_DATA_GEN` |

```groovy
BundleEvents.registry {
    it.items(myItemBuilder)
    it.blocks(myBlockBuilder)
    it.recipes(myRecipeBuilder)
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

Package: `de.luckymcdev.foundryengine.api.event.ClientEvents`

Only fires on the **client side**.

| Event | Description | Blueprint Node |
|---|---|---|
| `tick(cb)` | End of client tick | `EVENT_CLIENT_TICK` |
| `stopped(cb)` | Client fully stopped | `EVENT_CLIENT_STOPPED` |
| `stopping(cb)` | Client is stopping | `EVENT_CLIENT_STOPPING` |
| `chat(cb)` | Client sending a chat message | `EVENT_CHAT_MESSAGE` |
| `keyMappings(cb)` | Register key mappings | — |
| `renderGui(cb)` | After GUI is rendered | `EVENT_RENDER_GUI` |
| `renderGuiLayer(cb)` | After a GUI layer is rendered | — |
| `renderHand(cb)` | Hand rendering | — |
| `renderAfterLevel(cb)` | After level rendering | — |
| `loggedIn(cb)` | Client logged into a server | `EVENT_CLIENT_LOGGED_IN` |
| `loggedOut(cb)` | Client logged out | `EVENT_CLIENT_LOGGED_OUT` |

---

## CommandEvents

Package: `de.luckymcdev.foundryengine.api.event.CommandEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `register(cb)` | Register server commands | `EVENT_COMMANDS` |
| `registerClient(cb)` | Register client-side commands | `EVENT_COMMANDS_CLIENT` |

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

Package: `de.luckymcdev.foundryengine.api.event.EntityEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `joinLevel(cb)` | Entity enters a level | `EVENT_ENTITY_JOIN_LEVEL` |
| `death(cb)` | Living entity dies | `EVENT_LIVING_DEATH` |
| `drops(cb)` | Entity drops items on death | `EVENT_LIVING_DROPS` |
| `hurt(cb)` | Entity takes damage (post) | `EVENT_LIVING_HURT` |
| `spawned(cb)` | Entity spawned into world | `EVENT_ENTITY_JOIN_LEVEL` |
| `checkSpawn(cb)` | Check entity spawn conditions | `EVENT_ENTITY_JOIN_LEVEL` |

---

## GameEvents

Package: `de.luckymcdev.foundryengine.common.event.GameEvents`

Game session lifecycle events.

| Event | Description | Blueprint Node |
|---|---|---|
| `onStarting(cb)` | Game session starting | — |
| `onStarted(cb)` | Game session started | — |
| `onStopping(cb)` | Game session stopping | — |
| `onStopped(cb)` | Game session stopped | — |

---

## ItemEvents

Package: `de.luckymcdev.foundryengine.api.event.ItemEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `pickedUp(cb)` | Item picked up by player | `EVENT_ITEM_PICKUP` |
| `destroyed(cb)` | Item is destroyed (used up) | `EVENT_ITEM_DESTROY` |
| `rightClicked(cb)` | Player right-clicks with item | `EVENT_ITEM_RIGHT_CLICK` |
| `crafted(cb)` | Item crafted | `EVENT_ITEM_CRAFTED` |
| `dropped(cb)` | Item thrown/dropped | `EVENT_ITEM_DROPPED` |
| `foodEaten(cb)` | Food consumption finished | `EVENT_ITEM_FOOD_EATEN` |
| `smelted(cb)` | Item smelted in furnace | `EVENT_ITEM_SMELTED` |
| `dynamicTooltips(cb)` | Item tooltip rendering | `EVENT_ITEM_TOOLTIP` |
| `entityInteracted(cb)` | Player interacts entity with item | `EVENT_ITEM_ENTITY_INTERACT` |
| `firstLeftClicked(cb)` | Left-click on empty space | `EVENT_ITEM_FIRST_LEFT_CLICK` |
| `firstRightClicked(cb)` | Right-click on empty space | `EVENT_ITEM_FIRST_RIGHT_CLICK` |
| `modification(cb)` | Modify item behavior post-registration | — |

> **⚠️ ItemModificationEvent is unstable.** The modification API has known issues and may not produce the expected results in all cases. Use with caution.

```groovy
ItemEvents.modification { it -> it.setMaxStackSize(64) }
```

The `it` parameter exposes: `setMaxStackSize`, `setMaxDamage`, `setUnbreakable`, `setFood`, `setTool`, `setAttributeModifiers`.

---

## LevelEvents

Package: `de.luckymcdev.foundryengine.api.event.LevelEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `load(cb)` | Level/dimension is loaded | `EVENT_LEVEL_LOAD` |
| `unload(cb)` | Level/dimension is unloaded | `EVENT_LEVEL_UNLOAD` |
| `save(cb)` | Level data is saved | `EVENT_LEVEL_SAVE` |
| `tick(cb)` | End of level tick | `EVENT_LEVEL_TICK` |
| `beforeExplosion(cb)` | Before an explosion detonates | `EVENT_BEFORE_EXPLOSION` |
| `afterExplosion(cb)` | After an explosion detonates | `EVENT_AFTER_EXPLOSION` |

---

## NetworkEvents

Package: `de.luckymcdev.foundryengine.api.event.NetworkEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `login(cb)` | Player network login | `EVENT_NETWORK_LOGIN` |
| `logout(cb)` | Player network logout | `EVENT_NETWORK_LOGOUT` |

---

## PlayerEvents

Package: `de.luckymcdev.foundryengine.api.event.PlayerEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `loggedIn(cb)` | Player logged in | `EVENT_PLAYER_LOGGED_IN` |
| `loggedOut(cb)` | Player logged out | `EVENT_PLAYER_LOGGED_OUT` |
| `tick(cb)` | End of player tick | `EVENT_PLAYER_TICK` |
| `chat(cb)` | Player sends chat message | `EVENT_PLAYER_CHAT` |
| `advancement(cb)` | Player earns advancement | `EVENT_PLAYER_ADVANCEMENT` |
| `chestClosed(cb)` | Player closes a container | `EVENT_CHEST_CLOSED` |
| `chestOpened(cb)` | Player opens a container | `EVENT_CHEST_OPENED` |
| `respawned(cb)` | Player respawns | `EVENT_PLAYER_RESPAWNED` |
| `decorateChat(cb)` | Decorate received chat messages | `EVENT_DECORATE_CHAT` |

---

## RecipeEvents

Package: `de.luckymcdev.foundryengine.api.event.RecipeEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `recipesReceived(cb)` | Client received recipe list | `EVENT_RECIPE_VIEWER_UPDATED` |
| `modifyRecipes(cb)` | Modify recipe JSONs at reload | — |

---

## ServerEvents

Package: `de.luckymcdev.foundryengine.api.event.ServerEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `aboutToStart(cb)` | Server about to start | `EVENT_SERVER_ABOUT_TO_START` |
| `started(cb)` | Server has started | `EVENT_SERVER_STARTED` |
| `starting(cb)` | Server is starting | `EVENT_SERVER_STARTING` |
| `stopped(cb)` | Server has stopped | `EVENT_SERVER_STOPPED` |
| `stopping(cb)` | Server is stopping | `EVENT_SERVER_STOPPING` |
| `tick(cb)` | End of server tick | `EVENT_SERVER_TICK` |
| `tags(cb)` | Game tags were updated/reloaded | `EVENT_SERVER_TAGS` |

---

## SlotEvents

Package: `de.luckymcdev.foundryengine.common.event.SlotEvents`

| Event | Description | Blueprint Node |
|---|---|---|
| `modification(cb)` | Container menu opened or screen initialized | — |

Fired with `AbstractContainerMenu` when a container is opened or a screen initializes.

---

## StageEvents

Package: `de.luckymcdev.foundryengine.common.event.StageEvents`

Game stage progression events.

| Event | Description | Blueprint Node |
|---|---|---|
| `adding(cb)` | Before a stage is added (cancellable) | — |
| `removing(cb)` | Before a stage is removed (cancellable) | — |
| `added(cb)` | After a stage has been added | — |
| `removed(cb)` | After a stage has been removed | — |

---

## Using Events in Groovy Scripts

```groovy
package mybundle

import de.luckymcdev.foundryengine.api.event.*

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

## Internal / Addon Events

These events are not exposed via the `api.event` package but are available for Java addon developers using NeoForge's event bus directly.

### RegisterPanelEvent

Package: `de.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent`

Fired to register custom panels in the built-in Dear ImGui editor.

```java
@SubscribeEvent
public void onRegisterPanel(RegisterPanelEvent event) {
    event.register(new MyCustomPanel());
}
```

### RegisterRenderingStuffEvent

Package: `de.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent`

`@ApiStatus.Experimental` — Fired to register custom renderers, OBJ models, etc.

```java
@SubscribeEvent
public void onRegisterRendering(RegisterRenderingStuffEvent event) {
    ResourceManager manager = event.getResourceManager();
}
```

### RegisterKeyBindingEvent

Package: `de.luckymcdev.foundryengine.client.util.key.RegisterKeyBindingEvent`

Fired to register custom key bindings with the engine's key binding manager.

```java
@SubscribeEvent
public void onRegisterKeyBindings(RegisterKeyBindingEvent event) {
    event.register(new KeyBinding("key.mybind", GLFW.GLFW_KEY_P, "My Mod"));
}
```

### TitleScreenModifyEvent

Package: `de.luckymcdev.foundryengine.common.event.modification.TitleScreenModifyEvent`

`ICancellableEvent` — Fired when title screen buttons are being created. Can cancel `SINGLEPLAYER`, `MULTIPLAYER`, or `REALMS` buttons.

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

## See Also

- [Builders](builders) — Creating content that can be registered
