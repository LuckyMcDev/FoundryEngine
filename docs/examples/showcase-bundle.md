# Showcase Bundle

The **Showcase Bundle** (`ExampleBundles/showcase/`) is a complete bundle that demonstrates most FoundryEngine features. Each system lives in its own entrypoint file so you can use them as independent references.

## What it shows

| Feature          | File                  | What it does                                                         |
|------------------|-----------------------|----------------------------------------------------------------------|
| **Items**        | `CommonEntrypoint`    | 4 items with lore, callbacks, food components, and inventory ticking |
| **Blocks**       | `CommonEntrypoint`    | 3 blocks: glowing stone, fire trap (burns entities), invisible light |
| **Recipes**      | `CommonEntrypoint`    | Shaped, shapeless, and smelting recipes                              |
| **Sounds**       | `CommonEntrypoint`    | Custom sound with subtitle and range                                 |
| **Particles**    | `CommonEntrypoint`    | Sparkle particle with animated color and scale                       |
| **Events**       | `CommonEntrypoint`    | `PlayerEvents.tick` and `BlockEvents.broken`                         |
| **Areas**        | `AreaEntrypoint`      | Custom enter/leave/tick modules and an area preset                   |
| **Waypoints**    | `WaypointEntrypoint`  | 3 waypoints registered on first player join                          |
| **Stages**       | `StageEntrypoint`     | 3 progression stages with `adding`/`added` listeners                 |
| **Dialogue**     | `DialogueEntrypoint`  | Branching dialogue tree with actions and farewell trigger            |
| **Commands**     | `CommandEntrypoint`   | `/showcase hello`, `stage`, `list_waypoints`, `dialogue`             |
| **Game Session** | `LifecycleEntrypoint` | Auto-starting session with server tick broadcasts                    |
| **Lifecycle**    | `LifecycleEntrypoint` | Player join/leave logging, entity death logging                      |

## Bundle structure

```
showcase/
├── showcase.bundles.toml
├── scripts/
│   ├── common/
│   │   └── showcase/
│   │       └── CommonEntrypoint.groovy      # Items, blocks, recipes, sounds, particles, events
│   └── server/
│       └── showcase/
│           ├── AreaEntrypoint.groovy         # Areas (modules + preset)
│           ├── WaypointEntrypoint.groovy     # Waypoints
│           ├── StageEntrypoint.groovy        # Game stages
│           ├── DialogueEntrypoint.groovy     # Dialogue tree
│           ├── CommandEntrypoint.groovy      # Custom commands
│           └── LifecycleEntrypoint.groovy    # Game session + general listeners
├── assets/showcase/
└── data/showcase/
```

## Using the showcase

1. Ensure FoundryEngine is installed
2. Start the game (the bundle is in `ExampleBundles/` and gets copied to run directories)
3. Type `/showcase hello` to verify the bundle loaded
4. Type `/showcase dialogue` to start the welcome dialogue
5. Type `/showcase list_waypoints` to see registered waypoints
6. Walk through a healing zone area to see enter/leave/tick messages

## Key patterns

### Registration flow

All content is registered inside a `BundleEvents.registry` block:

```groovy
BundleEvents.registry {
    it.items(item1, item2)
    it.blocks(block1, block2)
    it.recipes(recipe1, recipe2)
    it.sounds(sound)
    it.particles(particle)
}
```

### Event listeners

Events use static `EventCallback` methods:

```groovy
PlayerEvents.tick { event ->
    // Runs every tick for every player
}

BlockEvents.broken { event ->
    // Runs when a block is broken
}
```

### Server-only features

Areas, waypoints, stages, dialogue, commands, and game sessions are registered in `scripts/server/` — they only run on the server side. Each system is isolated in its own entrypoint for clarity.

### Multi-entrypoint pattern

The bundle system loads every `.groovy` file that implements `BundleEntrypoint` inside `scripts/`. You can split your code across as many files as you want — each one gets `onLoad()` called independently.

## Next steps

- Browse the [examples index](../examples/) for more code snippets
- Read the [systems documentation](../systems/) for deep dives into each feature
- Check the test bundle (`ExampleBundles/testbundle/`) for additional reference code
