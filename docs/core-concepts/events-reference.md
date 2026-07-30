# Events Reference

All events are in package `de.luckymcdev.foundryengine.common.event`.

## BundleEvents

The most important events for bundle creators.

| Method                     | What it does                                                             |
|----------------------------|--------------------------------------------------------------------------|
| `registry(cb)`             | Register items, blocks, sounds, particles, recipes, block entities, tags |
| `commonSetup(cb)`          | FML common setup phase                                                   |
| `clientSetup(cb)`          | FML client-only setup                                                    |
| `dedicatedServerSetup(cb)` | Dedicated server setup                                                   |
| `postInit(cb)`             | After all mods initialize                                                |
| `dataGen(cb)`              | Bundle data generation                                                   |
| `custom(EventClass, cb)`   | Listen for any NeoForge event                                            |

## PlayerEvents

| Method            | Data you get      |
|-------------------|-------------------|
| `loggedIn(cb)`    | Player            |
| `loggedOut(cb)`   | Player            |
| `tick(cb)`        | Player            |
| `chat(cb)`        | Chat event        |
| `advancement(cb)` | Advancement event |
| `chestOpened(cb)` | Container event   |
| `chestClosed(cb)` | Container event   |
| `respawned(cb)`   | Player            |

## BlockEvents

| Method                 | Data you get              |
|------------------------|---------------------------|
| `broken(cb)`           | pos, player, level, state |
| `placed(cb)`           | pos, player, level, state |
| `neighborNotify(cb)`   | pos, level, state         |
| `leftClicked(cb)`      | pos, player, hand         |
| `rightClicked(cb)`     | pos, player, hand         |
| `farmlandTrampled(cb)` | pos, level, entity        |
| `modification(cb)`     | Block properties builder  |

## EntityEvents

| Method          | Data you get          |
|-----------------|-----------------------|
| `joinLevel(cb)` | Entity, level         |
| `death(cb)`     | Entity, source        |
| `drops(cb)`     | Drops event           |
| `hurt(cb)`      | Entity, damage source |

## ItemEvents

| Method                  | Data you get            |
|-------------------------|-------------------------|
| `pickedUp(cb)`          | Player, stack           |
| `destroyed(cb)`         | Item destroyed event    |
| `rightClicked(cb)`      | Player, hand            |
| `crafted(cb)`           | Player, stack           |
| `dropped(cb)`           | Player, stack           |
| `foodEaten(cb)`         | Player, stack           |
| `smelted(cb)`           | Player, stack           |
| `dynamicTooltips(cb)`   | Stack, tooltip lines    |
| `entityInteracted(cb)`  | Player, entity, hand    |
| `firstLeftClicked(cb)`  | Player, hand            |
| `firstRightClicked(cb)` | Player, hand            |
| `modification(cb)`      | Item properties builder |

## ServerEvents

| Method             | What it signals          |
|--------------------|--------------------------|
| `aboutToStart(cb)` | Server is about to start |
| `started(cb)`      | Server has started       |
| `starting(cb)`     | Server is starting       |
| `stopped(cb)`      | Server has stopped       |
| `stopping(cb)`     | Server is stopping       |
| `tick(cb)`         | End of server tick       |
| `tags(cb)`         | Tags were updated        |

## ClientEvents

Client-only events.

| Method                 | What it signals                |
|------------------------|--------------------------------|
| `tick(cb)`             | End of client tick             |
| `stopped(cb)`          | Client fully stopped           |
| `stopping(cb)`         | Client is stopping             |
| `chat(cb)`             | Client sending a chat message  |
| `chatReceived(cb)`     | Client received a chat message |
| `keyMappings(cb)`      | Register key mappings          |
| `renderGui(cb)`        | After GUI is rendered          |
| `renderGuiLayer(cb)`   | After a GUI layer is rendered  |
| `renderHand(cb)`       | Before hand is rendered        |
| `renderAfterLevel(cb)` | After the level is rendered    |
| `loggedIn(cb)`         | Client logged into a server    |
| `loggedOut(cb)`        | Client logged out              |

## Other event classes

| Class            | Events                                                                |
|------------------|-----------------------------------------------------------------------|
| `LevelEvents`    | `load`, `unload`, `save`, `tick`, `beforeExplosion`, `afterExplosion` |
| `NetworkEvents`  | `onCustomDataReceived`                                                |
| `CommandEvents`  | `register`, `registerClient`                                          |
| `RecipeEvents`   | `recipesReceived`, `modifyRecipes`                                    |
| `SlotEvents`     | `modification`                                                        |
| `StageEvents`    | `adding`, `removing`, `added`, `removed`                              |
| `DialogueEvents` | `onStarted`, `onAdvanced`, `onOptionSelected`, `onEnded`              |
| `GameEvents`     | `onStarting`, `onStarted`, `onStopping`, `onStopped`                  |

## Next

- [Events Guide](events-guide.md) — how to use events
- [Registration](registration.md) — register your content
