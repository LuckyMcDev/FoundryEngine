# What is a Bundle?

A **bundle** packages content in FoundryEngine. It combines scripts, textures, models, and data files in a single folder. No Java compilation or build tools needed.

## Folder structure

A bundle looks like this:

```
my-bundle/
├── my-bundle.bundles.toml      # The "ID card" of your bundle
├── scripts/
│   ├── common/                 # Shared logic (items, blocks, recipes)
│   ├── client/                 # Client-only code (rendering, keybinds)
│   └── server/                 # Server-only code (commands, data)
├── assets/                     # Resource pack (textures, models, sounds)
│   └── my_namespace/
│       ├── textures/
│       ├── models/
│       └── sounds/
└── data/                       # Data pack (recipes, loot tables, tags)
    └── my_namespace/
        ├── recipes/
        └── loot_tables/
```

## What can a bundle contain?

| Content               | How to create it                  |
|-----------------------|-----------------------------------|
| Items                 | Use `ItemBuilder` in a script     |
| Blocks                | Use `BlockBuilder` in a script    |
| Recipes (all 9 types) | Use `RecipeBuilder` in a script   |
| Sounds                | Use `SoundBuilder` in a script    |
| Particles             | Use `ParticleBuilder` in a script |
| Events                | Subscribe to events in a script   |
| Commands              | Register commands in a script     |
| Dimensions            | Use the instanced worlds system   |
| Cutscenes             | Use the cutscene system           |
| Textures/models       | Place in `assets/` folder         |
| Data pack files       | Place in `data/` folder           |

## Bundle lifecycle

1. **Discovery** — FoundryEngine reads bundles from `FoundryEngine/bundles/` when the game starts
2. **Loading** — Each bundle's entrypoint script runs `onLoad()`
3. **Registration** — `BundleEvents.registry` registers items, blocks, recipes, sounds, particles
4. **Runtime** — Events fire, scripts run, your content works
5. **Unload** — `/engine reload` triggers `onUnload()` then re-discovers everything

## Distribution

Bundles are distributed as **folders** (no ZIP needed). Users place them in `FoundryEngine/bundles/` and run `/engine reload`.

## Next

- [Bundle Manifest](bundle-manifest.md) — the `.bundles.toml` file explained
- [Your First Bundle](../getting-started/first-bundle.md) — step-by-step tutorial
