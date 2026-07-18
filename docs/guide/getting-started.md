# Getting started: your first bundle

This tutorial walks you through creating a bundle that adds a custom item and a crafting recipe without writing any Java.

If you haven't read about [Bundles](/concepts/core/bundles) yet, start there to understand the core concept.

## What is a bundle?

A **bundle** is a self-contained mod packaged as a folder. It combines Groovy scripts, resource pack files, and data pack files. No Java compilation, no Gradle build. Just plain files and a reload command.

Bundles can register:

- Custom items, blocks, recipes, sounds, and particles
- Event listeners (player join, block break, entity death, etc.)
- Custom commands
- Runtime dimensions and cutscenes

## Prerequisites

- Minecraft 26.1.x with NeoForge 26.1.x and FoundryEngine installed ([Installation Guide](index))

## Choose a workspace

You have two options. This tutorial uses the **in-game folder** approach (no build tools needed).

| Approach         | Setup Effort | Reload Speed               | Version Control |
|------------------|--------------|----------------------------|-----------------|
| In-game folder   | None         | Instant (`/engine reload`) | Manual          |
| Template project | Gradle setup | Build + deploy             | Built-in        |

See [Workspaces](workspaces) for a detailed comparison.

## Create the bundle folder

Navigate to your Minecraft directory and create a folder for your bundle:

```
.minecraft/FoundryEngine/bundles/my-first-bundle/
```

Inside, create the following structure:

```
my-first-bundle/
├── my-first-bundle.bundles.toml
└── scripts/
    └── common/
        └── my_first_bundle/
            └── Entrypoint.groovy
```

Scripts inside each side folder must be in a sub-package matching your bundle namespace. For example, if your `bundleId` is `my_first_bundle`, scripts go under `scripts/common/my_first_bundle/`.

## Write the bundle manifest

Create `my-first-bundle.bundles.toml`:

```toml
[[bundles]]
bundleId = "my_first_bundle"
version = "0.1.0"
displayName = "My First Bundle"
displayURL = "https://example.com"
authors = "YourName"
description = "My very first FoundryEngine bundle!"
dependencies = [
    "mod:neoforge@26.1.0.1-beta"
]
```

### Manifest fields

| Field          | Description                                              |
|----------------|----------------------------------------------------------|
| `bundleId`     | Unique identifier (lowercase, underscores, no spaces)    |
| `version`      | Semantic version (e.g. `0.1.0`)                          |
| `displayName`  | Human-readable name shown in the mods menu               |
| `displayURL`   | Optional link to your project page                       |
| `authors`      | Your name or team name                                   |
| `description`  | Shown in the mods menu                                   |
| `dependencies` | Required mods and bundles (`"mod:..."` / `"bundle:..."`) |

## Create the entrypoint script

The entrypoint connects the engine to your Groovy code. Create `scripts/common/my_first_bundle/Entrypoint.groovy`:

```groovy
package my_first_bundle

import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.ItemLore

class Entrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        Identifier.fromNamespaceAndPath("my_first_bundle", path)
    }

    @Override
    void onLoad() {
        println "My First Bundle loaded!"

        BundleEvents.registry {
            it.items(MY_ITEM)
        }
    }

    @Override
    void onUnload() {
        println "My First Bundle unloaded!"
    }
}
```

This entrypoint prints a message when loaded and unloaded. Nothing more yet.

## Add a custom item

Add an item definition above `onLoad()`. Place these lines right after the `id()` helper and before `onLoad()`:

```groovy
private static final ItemBuilder MY_ITEM = ItemBuilder.create(id("my_item"))
    .component(DataComponents.LORE, new ItemLore(List.of(
        Component.literal("A shimmering gem,"),
        Component.literal("pulled from the void.")
    )))
    .component(DataComponents.RARITY, Rarity.UNCOMMON)
    .stacksTo(16)
```

This creates a gem-like item that:

- Has a custom lore description
- Shows as **Uncommon** rarity (blue text)
- Stacks up to 16

## Final entrypoint

Your complete `Entrypoint.groovy` should look like this:

```groovy
package my_first_bundle

import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.ItemLore

class Entrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        Identifier.fromNamespaceAndPath("my_first_bundle", path)
    }

    private static final ItemBuilder MY_ITEM = ItemBuilder.create(id("my_item"))
        .component(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("A shimmering gem,"),
            Component.literal("pulled from the void.")
        )))
        .component(DataComponents.RARITY, Rarity.UNCOMMON)
        .stacksTo(16)

    @Override
    void onLoad() {
        println "My First Bundle loaded!"

        BundleEvents.registry {
            it.items(MY_ITEM)
        }
    }

    @Override
    void onUnload() {
        println "My First Bundle unloaded!"
    }
}
```

## Test your bundle

### In-game folder

1. Save all files
2. Launch Minecraft
3. Run `/engine reload` in the chat

The mod will discover your bundle, compile the entrypoint, and register the item and recipe. You should see `My First Bundle loaded!` in the log.

Open your creative inventory. Your item appears under the **FoundryEngine** tab. Open the crafting table and arrange two diamonds vertically with a stick below to craft it.

### Template project

If you're using the [template project](workspaces):

```bash
./gradlew deployBundle
./gradlew runClient
```

## Complete bundle structure

```
.minecraft/FoundryEngine/bundles/my-first-bundle/
├── my-first-bundle.bundles.toml
└── scripts/
    └── common/
        └── my_first_bundle/
            └── Entrypoint.groovy
```

As your bundle grows, you can add:

```
my-first-bundle/
├── my-first-bundle.bundles.toml
├── scripts/
│   ├── common/          # Shared logic (items, blocks, recipes)
│   │   └── my_first_bundle/
│   │       └── Entrypoint.groovy
│   ├── client/          # Client-only logic (rendering, keybinds)
│   │   └── my_first_bundle/
│   │       └── ClientEntrypoint.groovy
│   └── server/          # Server-only logic (commands, data)
│       └── my_first_bundle/
│           └── ServerEntrypoint.groovy
├── assets/              # Resource pack (textures, models, sounds, lang)
│   └── my_first_bundle/
│       ├── textures/
│       ├── models/
│       └── sounds/
├── data/                # Data pack (recipes, loot tables, tags)
│   └── my_first_bundle/
│       ├── recipes/
│       └── loot_tables/
```

## Troubleshooting

| Symptom                     | Cause                 | Fix                                            |
|-----------------------------|-----------------------|------------------------------------------------|
| `/engine reload` not found  | Mod not installed     | Verify FoundryEngine is in `mods/`             |
| Bundle not loading          | TOML syntax error     | Check `[[bundles]]` header and braces          |
| `ClassNotFoundException`    | Import missing        | Add the missing `import` statement             |
| Changes not appearing       | Script not recompiled | Run `/engine reload` again                     |
| Item missing from inventory | Not registered        | Check `BundleEvents.registry {}` in `onLoad()` |

## Next steps

- [Workspaces](workspaces) -- Compare in-game folder vs template project
- [Builders](/concepts/core/builders) -- Add blocks, sounds, particles, and more recipe types
- [Events](/concepts/core/events) -- React to player join, block break, and other game events
- [Scripts](/concepts/core/scripts) -- Deep dive into the Groovy scripting system
- [Examples](/examples/) -- Complete working examples
