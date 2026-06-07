# Concepts: Bundles

A **Bundle** is the fundamental packaging unit in Foundry Engine. It is a self-contained mod that combines
a [Resource Pack](https://minecraft.wiki/w/Resource_pack),
a [Data Pack](https://minecraft.wiki/w/Data_pack), [Scripts](scripts.md), and optionally custom items, blocks,
recipes, sounds, and particles — all **without compiling any Java code**.

## Folder Structure

The built form of a bundle looks like this:

```
bundleName/
├── bundleName.bundles.toml      # Bundle metadata & dependencies
├── scripts/
│   ├── server/                  # Server-side Groovy scripts
│   ├── client/                  # Client-side Groovy scripts
│   └── common/                  # Shared Groovy scripts (both sides)
├── assets/                      # Resource pack (textures, models, sounds, lang)
│   └── <namespace>/
│       ├── textures/
│       ├── models/
│       ├── sounds/
│       └── lang/
├── data/                        # Data pack (recipes, loot tables, tags)
│   └── <namespace>/
│       ├── recipes/
│       ├── loot_tables/
│       └── tags/
└── blueprints/                  # Blueprint .febp files (visual scripting)
```

## Bundle Manifest (TOML)

Each bundle requires a `.bundles.toml` file with metadata and dependencies:

```toml
[[bundles]]
bundleId = "mybundle"
version = "1.0.0"
displayName = "My Bundle"
displayURL = "https://example.com"
authors = "YourName"
description = '''A description of your bundle'''
dependencies = [
    "mod:neoforge@26.1.0.1-beta",
    "bundle:some-library@1.0.0"
]
```

### Fields

| Field | Description |
|-------|-------------|
| `bundleId` | Unique identifier (lowercase, no spaces) |
| `version` | Semantic version string |
| `displayName` | Human-readable name (shown in mods menu) |
| `displayURL` | Optional project URL |
| `authors` | Author name(s) |
| `description` | Description shown in mods menu |
| `dependencies` | Array of `"mod:..."` or `"bundle:..."` dependency strings |

## Bundle Types

Bundles can define the following content types via the [Registry System](registries.md):

- **Items** — Custom items with properties, tooltips, and behaviors
- **Blocks** — Custom blocks with collision, light emission, sound type, and more
- **Recipes** — Crafting, smelting, and other recipe types
- **Sounds** — Custom sound events with definitions
- **Particles** — Custom particle types (keyframe-driven)

## Lifecycle

1. **Discovery** — Bundles are discovered from `.minecraft/FoundryEngine/bundles/` or from the template project's build output
2. **Registration** — `BundleEvents.registry {}` is called to register all items, blocks, recipes, etc.
3. **Loading** — Entrypoint scripts fire `onLoad()` for each bundle
4. **Runtime** — Events, blueprints, and scripts operate during gameplay
5. **Unloading** — `/engine reload` triggers `onUnload()` and re-discovers bundles

## Distribution

Bundles are distributed as folders (no ZIP needed). Users place them in `.minecraft/FoundryEngine/bundles/`.

## See Also

- [Scripts](scripts.md) — How to write bundle scripts
- [Entrypoints](entrypoint.md) — Script entry points
- [Registries](registries.md) — Registering items, blocks, and more
- [Dependencies](dependencies.md) — Bundle and mod dependency syntax
- [Workspaces](workspaces.md) — Setting up a bundle development workspace
- [Events](events.md) — Reacting to game events from your bundle

