# Concepts: Events

Foundry Engine exposes a rich event system that lets you react to nearly everything that happens in the game.
Events can be subscribed to from **Groovy scripts**, **Blueprints**, or **Java addons**.

All API events are in the package `de.luckymcdev.foundryengine.api.event` and follow a consistent pattern:

```groovy
// Groovy example — subscribe to any event
BlockEvents.broken {
    println it.pos
}
```

## Event System Architecture

Foundry Engine wraps NeoForge's event bus with a **layered bridge**:

```
NeoForge Event Bus
    ↕ (listener forwards)
Foundry Engine EventGroupHolder
    ↕ (dispatches to)
    ├── Java/Groovy callbacks (EventCallback<T>)
    └── Blueprint visual scripting nodes
```

Each API event class (e.g. `BlockEvents`) contains:
- **Static `EventGroupHolder` fields** — one per event type, holding both Java callbacks and Blueprint node links
- **Static registration methods** — e.g. `BlockEvents.broken(callback)` adds your callback
- **Internal `Internal` class** — listens on NeoForge's bus and forwards to the `EventGroupHolder`

### EventCallback

A `@FunctionalInterface` with a single method:

```java
void execute(T event);
```

In Groovy this can be a closure (using the implicit `it` parameter) or a lambda: `it -> ...`.

### EventGroupHolder

Wraps an `EventGroup` (thread-safe list of callbacks) plus an optional Blueprint node ID and context mapper.
When an event fires, it:
1. Calls all registered Java/Groovy `EventCallback` listeners sequentially
2. If a Blueprint node ID is set, maps the event to a `Map<String, Object>` context and executes matching Blueprint graphs

### Custom Events

You can listen for **any** NeoForge event class, even ones Foundry Engine doesn't explicitly wrap:

```groovy
BundleEvents.custom(SomeEvent.class, {
    println "Custom event fired"
})
```

You can also provide a blueprint context mapper for custom events:

```groovy
BundleEvents.custom(SomeEvent.class, {
    println "Custom event fired"
}, { ["key": it.someValue] })
```

---

## Event Reference

### AreaEvents

Package: `de.luckymcdev.foundryengine.api.event.AreaEvents`

Fires when entities interact with custom **Area** zones.

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `areaEnter(cb)` | `AreaEvent.AreaEnterEvent` | Entity enters an area | `EVENT_AREA_ENTER` |
| `areaLeave(cb)` | `AreaEvent.AreaLeaveEvent` | Entity leaves an area | `EVENT_AREA_LEAVE` |
| `areaTick(cb)` | `AreaEvent.AreaTickEvent` | Tick while entity is inside area | `EVENT_AREA_TICK` |

**Context available (common to all three):**
- `area` (`Area`) — The area zone
- `entities` (`List<Entity>`) — Entities involved

**What you can do:**
- Trigger cutscenes when a player enters a region
- Apply status effects while inside an area
- Remove mobs that wander into a restricted zone
- Track player presence in puzzle rooms

---

### BlockEvents

Package: `de.luckymcdev.foundryengine.api.event.BlockEvents`

Fires for block interactions, placement, and destruction.

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `broken(cb)` | `BreakBlockEvent` | A block is broken | `EVENT_BLOCK_BROKEN` |
| `placed(cb)` | `BlockEvent.EntityPlaceEvent` | A block is placed | `EVENT_BLOCK_PLACED` |
| `neighborNotify(cb)` | `BlockEvent.NeighborNotifyEvent` | Block neighbor update | - |
| `leftClicked(cb)` | `PlayerInteractEvent.LeftClickBlock` | Player left-clicks a block | `EVENT_BLOCK_LEFT_CLICKED` |
| `rightClicked(cb)` | `PlayerInteractEvent.RightClickBlock` | Player right-clicks a block | `EVENT_BLOCK_RIGHT_CLICKED` |
| `farmlandTrampled(cb)` | `BlockEvent.FarmlandTrampleEvent` | Farmland is trampled | `EVENT_FARMLAND_TRAMPLED` |
| `modification(cb)` | `BlockModificationEvent` | Modify block behavior post-registration | - |

**Context available via Blueprint:**
- `block`, `pos`, `player`, `level`, `state` (varies by event)

**What you can do:**
- Custom block drops and loot
- Trigger events when specific blocks are broken/placed
- Prevent block breaking/placing in protected areas
- Add custom behavior on right-click (e.g. open a GUI)
- Modify block properties like collision, light emission, friction, sound type, explosion resistance, speed factor, and jump factor via `BlockModificationEvent`

**BlockModificationEvent** methods (builder pattern — chain them):

```groovy
BlockEvents.modification {
    it.hasCollision(false)
      .explosionResistance(1000.0f)
      .lightEmission(15)
      .soundType(SoundType.AMETHYST)
      .friction(0.1f)
      .speedFactor(0.5f)
      .jumpFactor(2.0f)
      .randomlyTicking(true)
}
```

---

### BundleEvents

Package: `de.luckymcdev.foundryengine.api.event.BundleEvents`

Core lifecycle events for the mod and bundles. **This is the most important event class for bundle developers.**

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `registry(cb)` | `RegistryEvent` | Register items, blocks, recipes, sounds, particles | `EVENT_REGISTRY` |
| `vanillaGame(cb)` | `VanillaGameEvent` | Vanilla game events (e.g. advancement) | `EVENT_VANILLA_GAME` |
| `commonSetup(cb)` | `FMLCommonSetupEvent` | FML common setup phase | `EVENT_COMMON_SETUP` |
| `clientSetup(cb)` | `FMLClientSetupEvent` | FML client-only setup | `EVENT_CLIENT_SETUP` |
| `dedicatedServerSetup(cb)` | `FMLDedicatedServerSetupEvent` | Dedicated server setup | `EVENT_DEDICATED_SERVER_SETUP` |
| `postInit(cb)` | `InterModProcessEvent` | After all mods initialize | `EVENT_POST_INIT` |
| `serverAboutToStart(cb)` | `ServerAboutToStartEvent` | Server about to start | `EVENT_SERVER_ABOUT_TO_START` |
| `dataGen(cb)` | `BundleDataGenEvent` | Bundle data generation | `EVENT_DATA_GEN` |

**What you can do:**

**Registry** — Register your custom content:

```groovy
BundleEvents.registry {
    it.items(myItemBuilder)
    it.blocks(myBlockBuilder)
    it.recipes(myRecipeBuilder)
    it.particles(myParticleBuilder)
    it.sounds(mySoundBuilder)
}
```

**DataGen** — Add custom data providers:

```groovy
BundleEvents.dataGen {
    it.addProvider(myDataProvider)
}
```

**Custom Events** — Listen for any NeoForge `Event` subclass:

```groovy
BundleEvents.custom(LivingDeathEvent.class, {
    println "${it.entity} died"
})
```

---

### ClientEvents

Package: `de.luckymcdev.foundryengine.api.event.ClientEvents`

Only fires on the **client side**.

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `tick(cb)` | `ClientTickEvent.Post` | End of client tick | `EVENT_CLIENT_TICK` |
| `stopped(cb)` | `ClientStoppedEvent` | Client fully stopped | `EVENT_CLIENT_STOPPED` |
| `stopping(cb)` | `ClientStoppingEvent` | Client is stopping | `EVENT_CLIENT_STOPPING` |
| `chat(cb)` | `ClientChatEvent` | Client sending a chat message | `EVENT_CHAT_MESSAGE` |
| `keyMappings(cb)` | `RegisterKeyMappingsEvent` | Register key mappings | - |
| `renderGui(cb)` | `RenderGuiEvent.Post` | After GUI is rendered | `EVENT_RENDER_GUI` |
| `renderGuiLayer(cb)` | `RenderGuiLayerEvent.Post` | After a GUI layer is rendered | - |
| `renderHand(cb)` | `RenderHandEvent` | Hand rendering | - |
| `renderAfterLevel(cb)` | `RenderLevelStageEvent.AfterLevel` | After level rendering | - |
| `loggedIn(cb)` | `ClientPlayerNetworkEvent.LoggingIn` | Client logged into a server | `EVENT_CLIENT_LOGGED_IN` |
| `loggedOut(cb)` | `ClientPlayerNetworkEvent.LoggingOut` | Client logged out | `EVENT_CLIENT_LOGGED_OUT` |

**What you can do:**
- Render custom overlays / HUD elements via `renderGui`
- Add custom key bindings via `keyMappings`
- Intercept or modify chat messages
- Run per-frame update logic via `tick`
- Detect world join/leave on the client

---

### CommandEvents

Package: `de.luckymcdev.foundryengine.api.event.CommandEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `register(cb)` | `RegisterCommandsEvent` | Register server commands | `EVENT_COMMANDS` |
| `registerClient(cb)` | `RegisterClientCommandsEvent` | Register client-side commands | `EVENT_COMMANDS_CLIENT` |

**What you can do:**

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

### EntityEvents

Package: `de.luckymcdev.foundryengine.api.event.EntityEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `joinLevel(cb)` | `EntityJoinLevelEvent` | Entity enters a level | `EVENT_ENTITY_JOIN_LEVEL` |
| `death(cb)` | `LivingDeathEvent` | Living entity dies | `EVENT_LIVING_DEATH` |
| `drops(cb)` | `LivingDropsEvent` | Entity drops items on death | `EVENT_LIVING_DROPS` |
| `hurt(cb)` | `LivingDamageEvent.Post` | Entity takes damage (post) | `EVENT_LIVING_HURT` |
| `spawned(cb)` | `EntityJoinLevelEvent` | Entity spawned into world | `EVENT_ENTITY_JOIN_LEVEL` |
| `checkSpawn(cb)` | `EntityJoinLevelEvent` | Check entity spawn conditions | `EVENT_ENTITY_JOIN_LEVEL` |

**What you can do:**
- Custom death messages and effects
- Modify or cancel mob drops
- Add custom damage reactions
- Restrict which entities can enter a dimension
- Spawn particles on entity hurt/death

---

### ItemEvents

Package: `de.luckymcdev.foundryengine.api.event.ItemEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `pickedUp(cb)` | `ItemEntityPickupEvent.Post` | Item picked up by player | `EVENT_ITEM_PICKUP` |
| `destroyed(cb)` | `PlayerDestroyItemEvent` | Item is destroyed (used up) | `EVENT_ITEM_DESTROY` |
| `rightClicked(cb)` | `PlayerInteractEvent.RightClickItem` | Player right-clicks with item | `EVENT_ITEM_RIGHT_CLICK` |
| `crafted(cb)` | `PlayerEvent.ItemCraftedEvent` | Item crafted | `EVENT_ITEM_CRAFTED` |
| `dropped(cb)` | `ItemTossEvent` | Item thrown/dropped | `EVENT_ITEM_DROPPED` |
| `foodEaten(cb)` | `LivingEntityUseItemEvent.Finish` | Food consumption finished | `EVENT_ITEM_FOOD_EATEN` |
| `smelted(cb)` | `PlayerEvent.ItemSmeltedEvent` | Item smelted in furnace | `EVENT_ITEM_SMELTED` |
| `dynamicTooltips(cb)` | `ItemTooltipEvent` | Item tooltip rendering | `EVENT_ITEM_TOOLTIP` |
| `entityInteracted(cb)` | `PlayerInteractEvent.EntityInteract` | Player interacts entity with item | `EVENT_ITEM_ENTITY_INTERACT` |
| `firstLeftClicked(cb)` | `PlayerInteractEvent.LeftClickEmpty` | Left-click on empty space | `EVENT_ITEM_FIRST_LEFT_CLICK` |
| `firstRightClicked(cb)` | `PlayerInteractEvent.RightClickEmpty` | Right-click on empty space | `EVENT_ITEM_FIRST_RIGHT_CLICK` |
| `modification(cb)` | `ItemModificationEvent` | Modify item behavior post-registration | - |

**What you can do:**
- Custom item behavior on right-click (throwable items, wands, etc.)
- Dynamic tooltips (show stats, lore, cooldowns)
- Track item crafting/ smelting statistics
- Custom food effects
- Modify items via `ItemModificationEvent` (currently `getItem()` — more features planned)

---

### LevelEvents

Package: `de.luckymcdev.foundryengine.api.event.LevelEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `load(cb)` | `LevelEvent.Load` | Level/dimension is loaded | `EVENT_LEVEL_LOAD` |
| `unload(cb)` | `LevelEvent.Unload` | Level/dimension is unloaded | `EVENT_LEVEL_UNLOAD` |
| `save(cb)` | `LevelEvent.Save` | Level data is saved | `EVENT_LEVEL_SAVE` |
| `tick(cb)` | `LevelTickEvent.Post` | End of level tick | `EVENT_LEVEL_TICK` |
| `beforeExplosion(cb)` | `ExplosionEvent.Start` | Before an explosion detonates | `EVENT_BEFORE_EXPLOSION` |
| `afterExplosion(cb)` | `ExplosionEvent.Detonate` | After an explosion detonates | `EVENT_AFTER_EXPLOSION` |

**What you can do:**
- Per-dimension game rules
- Cancel or modify explosions
- Run scheduled tasks on level tick
- Save/load custom level data
- Detect when dimensions are created or removed

---

### NetworkEvents

Package: `de.luckymcdev.foundryengine.api.event.NetworkEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `login(cb)` | `PlayerEvent.PlayerLoggedInEvent` | Player network login | `EVENT_NETWORK_LOGIN` |
| `logout(cb)` | `PlayerEvent.PlayerLoggedOutEvent` | Player network logout | `EVENT_NETWORK_LOGOUT` |

**What you can do:**
- Track player connections
- Send welcome packets or MOTD
- Clean up player-specific data on disconnect

---

### PlayerEvents

Package: `de.luckymcdev.foundryengine.api.event.PlayerEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `loggedIn(cb)` | `PlayerEvent.PlayerLoggedInEvent` | Player logged in | `EVENT_PLAYER_LOGGED_IN` |
| `loggedOut(cb)` | `PlayerEvent.PlayerLoggedOutEvent` | Player logged out | `EVENT_PLAYER_LOGGED_OUT` |
| `tick(cb)` | `PlayerTickEvent.Post` | End of player tick | `EVENT_PLAYER_TICK` |
| `chat(cb)` | `ServerChatEvent` | Player sends chat message | `EVENT_PLAYER_CHAT` |
| `advancement(cb)` | `AdvancementEvent.AdvancementEarnEvent` | Player earns advancement | `EVENT_PLAYER_ADVANCEMENT` |
| `chestClosed(cb)` | `PlayerContainerEvent.Close` | Player closes a container | `EVENT_CHEST_CLOSED` |
| `chestOpened(cb)` | `PlayerContainerEvent.Open` | Player opens a container | `EVENT_CHEST_OPENED` |
| `respawned(cb)` | `PlayerEvent.PlayerRespawnEvent` | Player respawns | `EVENT_PLAYER_RESPAWNED` |
| `decorateChat(cb)` | `ClientChatReceivedEvent` | Decorate received chat messages | `EVENT_DECORATE_CHAT` |

**What you can do:**
- Per-player tick logic (mana regen, aura effects, flight checks)
- Chat filters, commands, or formatting via `decorateChat`
- Reward players on advancement
- Track container usage (chest shops, loot tracking)
- Respawn handling (give starter items, set spawn)

---

### RecipeEvents

Package: `de.luckymcdev.foundryengine.api.event.RecipeEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `recipesReceived(cb)` | `RecipesReceivedEvent` | Client received recipe list | `EVENT_RECIPE_VIEWER_UPDATED` |
| `modifyRecipes(cb)` | `ModifyRecipeJsonsEvent` | Modify recipe JSONs at reload | - |

**What you can do:**
- Dynamically add or remove recipes
- Modify recipe inputs/outputs on the fly
- React to recipe book updates on the client

---

### ServerEvents

Package: `de.luckymcdev.foundryengine.api.event.ServerEvents`

| Method | Event Type | Description | Blueprint Node |
|--------|-----------|-------------|----------------|
| `aboutToStart(cb)` | `ServerAboutToStartEvent` | Server about to start | `EVENT_SERVER_ABOUT_TO_START` |
| `started(cb)` | `ServerStartedEvent` | Server has started | `EVENT_SERVER_STARTED` |
| `starting(cb)` | `ServerStartingEvent` | Server is starting | `EVENT_SERVER_STARTING` |
| `stopped(cb)` | `ServerStoppedEvent` | Server has stopped | `EVENT_SERVER_STOPPED` |
| `stopping(cb)` | `ServerStoppingEvent` | Server is stopping | `EVENT_SERVER_STOPPING` |
| `tick(cb)` | `ServerTickEvent.Post` | End of server tick | `EVENT_SERVER_TICK` |
| `tags(cb)` | `TagsUpdatedEvent` | Game tags were updated/reloaded | `EVENT_SERVER_TAGS` |

**What you can do:**
- Initialize server-wide systems on startup
- Save data on server stop
- Per-tick global logic (economy, mob caps, weather)
- React to tag reloads
- Register commands, capabilities, or global game rules

---

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

`@ApiStatus.Experimental` — Fired to register rendering stuff (custom renderers, OBJ models, etc.).

```java
@SubscribeEvent
public void onRegisterRendering(RegisterRenderingStuffEvent event) {
    ResourceManager manager = event.getResourceManager();
    // Register custom renderers
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

Package: `de.luckymcdev.foundryengine.common.event.TitleScreenModifyEvent`

`ICancellableEvent` — Fired when title screen buttons are being created. Can cancel `SINGLEPLAYER`, `MULTIPLAYER`, or `REALMS` buttons.

```java
@SubscribeEvent
public void onTitleScreen(TitleScreenModifyEvent event) {
    if (event.getButtonType() == TitleScreenModifyEvent.ButtonType.REALMS) {
        event.setCanceled(true); // Remove realms button
    }
}
```

### GameStageEvent

Package: `de.luckymcdev.foundryengine.common.game.stage.GameStageEvent`

Abstract event — listen to subclasses:

| Subclass | Cancellable | Description |
|----------|-------------|-------------|
| `GameStageEvent.Add` | Yes | Before a stage is added to a player |
| `GameStageEvent.Remove` | Yes | Before a stage is removed from a player |
| `GameStageEvent.Added` | No | After a stage has been added |
| `GameStageEvent.Removed` | No | After a stage has been removed |

All four expose:
- `getStageName()` — the stage string
- `getPlayer()` — the affected player

**What you can do:**
- Gate content behind progression stages
- Cancel stage addition if prerequisites aren't met
- Trigger events when a player reaches a new stage
- Save/load stage data

---

## Using Events in Groovy Scripts

```groovy
package mybundle

import de.luckymcdev.foundryengine.api.event.*

class Entrypoint implements BundleEntrypoint {
    @Override
    void onLoad() {
        // Listen to events here
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
            // Register builders here
        }
    }
    
    @Override
    void onUnload() {
        // Clean up if needed
    }
}
```

## Using Events in Blueprints

Most API events are automatically exposed as Blueprint event nodes. Simply add the corresponding builtin node (e.g. `EVENT_BLOCK_BROKEN`) to your blueprint graph — the engine will connect the event to your node automatically.

See the [Blueprints](blueprints.md) documentation for more details on Blueprint scripting.
