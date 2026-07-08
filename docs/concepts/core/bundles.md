# Bundles

A **Bundle** is the fundamental packaging unit in FoundryEngine. It combines a resource pack, a data pack, Groovy scripts, blueprints, and optionally custom items, blocks, recipes, sounds, and particles without compiling any Java code.

## Folder Structure

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

```

## Bundle Manifest

Each bundle requires a `.bundles.toml` file with metadata and dependencies:

```toml
[[bundles]]
bundleId = "mybundle"
version = "1.0.0"
displayName = "My Bundle"
displayURL = "https://example.com"
authors = "YourName"
description = "A description of your bundle"
dependencies = [
    "mod:neoforge@26.1.0.1-beta",
    "bundle:some-library@1.0.0"
]
```

### Fields

| Field | Description |
|---|---|
| `bundleId` | Unique identifier (lowercase, no spaces) |
| `version` | Semantic version string |
| `displayName` | Human-readable name (shown in mods menu) |
| `displayURL` | Optional project URL |
| `authors` | Author name(s) |
| `description` | Description shown in mods menu |
| `dependencies` | Array of `"mod:..."` or `"bundle:..."` dependency strings |

## What Bundles Can Define

Bundles can define the following content types via the [Builder API](builders) and [Registry System](registries):

- **Items** — Custom items with properties, tooltips, and behaviors
- **Blocks** — Custom blocks with collision, light emission, sound type, and more
- **Recipes** — All 9 recipe types (shaped, shapeless, smelting, blasting, smoking, campfire cooking, stonecutting, smithing transform, smithing trim)
- **Sounds** — Custom sound events with file definitions
- **Particles** — Custom particle types with keyframe-driven animation

## Lifecycle

1. **Discovery** -- Bundles are read from `FoundryEngine/bundles/` in the run directory
2. **Registration** -- `BundleEvents.registry {}` registers all items, blocks, recipes, sounds, and particles
3. **Loading** -- Entrypoint scripts fire `onLoad()` for each bundle
4. **Runtime** -- Events, blueprints, and scripts operate during gameplay
5. **Unload** -- `/engine reload` triggers `onUnload()` and re-discovers bundles

## Distribution

Bundles are distributed as folders (no ZIP needed). Users place them in the `FoundryEngine/bundles/` directory.

## See also

- [Scripts and Entrypoints](scripts) -- How to write bundle scripts
- [Builders](builders) -- Creating items, blocks, recipes, sounds, and particles
- [Registries](registries) -- Registering custom content
- [Dependencies](dependencies) -- Bundle and mod dependency syntax
- [Events](events) -- Reacting to game events from your bundle
- [Sides](sides) -- Client/server separation for scripts
