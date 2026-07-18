# Your first bundle

This tutorial walks you through creating a bundle that adds a custom item — no Java required.

If you have not read about [What is a Bundle?](../core-concepts/what-is-a-bundle.md) yet, start there.

## What we are building

A magical gem item with:

- A custom name and lore text
- **Uncommon** rarity (blue text)
- Stacks up to 16

## Prerequisites

- Minecraft 26.1.x with NeoForge and FoundryEngine installed
- See the [Installation Guide](installation.md) if you have not done this yet

## Pick a workspace

You have two options. This tutorial uses the **in-game folder** approach (no build tools).

| Approach         | Setup        | Reload Speed               | Version Control |
|------------------|--------------|----------------------------|-----------------|
| In-game folder   | None         | Instant (`/engine reload`) | Manual          |
| Template project | Gradle setup | Build + deploy             | Built-in        |

See [Workspaces](workspaces.md) for a detailed comparison.

## Create your bundle folder

Go to your Minecraft directory and create:

```
.minecraft/FoundryEngine/bundles/my-first-bundle/
```

Inside, create this structure:

```
my-first-bundle/
├── my-first-bundle.bundles.toml
└── scripts/
    └── common/
        └── my_first_bundle/
            └── Entrypoint.groovy
```

> **Why `my_first_bundle`?** The script folder must match the `bundleId` in your manifest. The `bundleId` uses underscores (no spaces or dashes).

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

**What each field means:**

| Field          | Purpose                                                         |
|----------------|-----------------------------------------------------------------|
| `bundleId`     | Unique name for your bundle (lowercase, underscores, no spaces) |
| `version`      | Your bundle's version (e.g. `0.1.0`)                            |
| `displayName`  | Human-readable name shown in the mods menu                      |
| `displayURL`   | Optional link to your project page                              |
| `authors`      | Your name or team                                               |
| `description`  | Shown in the mods menu                                          |
| `dependencies` | Other mods or bundles your bundle needs                         |

## Create the entrypoint script

The entrypoint is the "main" file that FoundryEngine runs when your bundle loads.

Create `scripts/common/my_first_bundle/Entrypoint.groovy`:

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
    }

    @Override
    void onUnload() {
        println "My First Bundle unloaded!"
    }
}
```

**What this does:**

- `id()` is a helper that creates Minecraft resource identifiers (like `my_first_bundle:my_item`)
- `onLoad()` runs when your bundle is loaded
- `onUnload()` runs when your bundle is unloaded (e.g. on `/engine reload`)

## Add a custom item

Add an item definition before `onLoad()`:

```groovy
private static final ItemBuilder MY_ITEM = ItemBuilder.create(id("my_item"))
    .component(DataComponents.LORE, new ItemLore(List.of(
        Component.literal("A shimmering gem,"),
        Component.literal("pulled from the void.")
    )))
    .component(DataComponents.RARITY, Rarity.UNCOMMON)
    .stacksTo(16)
```

**What each line does:**

- `ItemBuilder.create(...)` — start building a new item
- `.component(DataComponents.LORE, ...)` — add a description tooltip
- `.component(DataComponents.RARITY, Rarity.UNCOMMON)` — make it blue-rarity
- `.stacksTo(16)` — limit stacks to 16 (default is 64)

Now register the item inside `onLoad()`:

```groovy
BundleEvents.registry {
    it.items(MY_ITEM)
}
```

This tells FoundryEngine: "this item exists, make it available in the game."

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
3. Run `/engine reload` in chat

You should see `My First Bundle loaded!` in the log. Open your creative inventory — your item appears under the **FoundryEngine** tab.

### Template project

If you are using the template project:

```bash
./gradlew deployBundle
./gradlew runClient
```

## Troubleshooting

| Problem                    | Likely cause          | Fix                                            |
|----------------------------|-----------------------|------------------------------------------------|
| `/engine reload` not found | Mod not installed     | Check FoundryEngine is in `mods/`              |
| Bundle not loading         | TOML syntax error     | Check `[[bundles]]` header and braces          |
| `ClassNotFoundException`   | Missing import        | Add the missing `import` statement             |
| Changes not appearing      | Script not recompiled | Run `/engine reload` again                     |
| Item not in inventory      | Not registered        | Check `BundleEvents.registry {}` in `onLoad()` |

## What's next

- [Workspaces](workspaces.md) — compare in-game folder vs template project
- [Creating Items](../core-concepts/creating-items.md) — add behaviors, food, tools
- [Creating Blocks](../core-concepts/creating-blocks.md) — add custom blocks
- [Events Guide](../core-concepts/events-guide.md) — react to player join, block break, etc.
- [Examples](../examples/) — complete working examples
