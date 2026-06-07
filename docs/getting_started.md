# Getting Started

This page walks you through setting up a bundle development workspace and creating your first bundle.

If you haven't read about [Bundles](concepts/bundles) yet, start there — they're the core concept.

## Choose a Workspace

You have two options. See [Workspaces](concepts/workspaces) for a full comparison.

### Option 1: In-Game Folder (Quick Start)

Create a folder directly in your Minecraft directory:

```
.minecraft/FoundryEngine/bundles/my-bundle/
```

Add your files (scripts, assets, data) and run `/engine reload` to see changes instantly. No build step required.

### Option 2: Template Project (Recommended)

Clone or generate from the [FoundryEngine Bundle Template](https://github.com/LuckyMcDev/FoundryEngineBundleTemplate):

```bash
git clone https://github.com/LuckyMcDev/FoundryEngineBundleTemplate my-bundle
cd my-bundle
```

This gives you Gradle build tasks, version control, and deployment automation.

## Configure Your Bundle

Edit the `.bundles.toml` manifest:

```toml
[[bundles]]
bundleId = "mybundle"
version = "0.0.1"
displayName = "My Bundle"
displayURL = "https://example.com"
authors = "YourName"
description = "My first FoundryEngine bundle!"
dependencies = [
   "mod:neoforge@26.1.0.1-beta"
]
```

## Create an Entrypoint

Create `scripts/common/Entrypoint.groovy`:

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.api.event.BundleEvents
import de.luckymcdev.foundryengine.api.event.ServerEvents
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import net.minecraft.world.item.Rarity
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemLore
import net.minecraft.network.chat.Component

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        println "My bundle loaded!"

        BundleEvents.registry {
            it.items(myItem)
        }

        ServerEvents.started {
            println "Server is ready!"
        }
    }

    @Override
    void onUnload() {
        println "My bundle unloaded!"
    }
}
```

## Build and Test

- **In-game folder**: Save files, run `/engine reload`, and test immediately
- **Template project**: Run `gradlew deployBundle` then `gradlew runClient`

## Distribute Your Bundle

Share the entire bundle folder. Users place it in `.minecraft/FoundryEngine/bundles/`.

With the template project, run `gradlew deployBundle` and copy the output from `build/bundles/`.

## Next Steps

- [Builders](concepts/builders) — Register items, blocks, recipes, sounds, and particles
- [Events](concepts/events) — React to game events
- [Blueprints](concepts/blueprints) — Visual scripting without code
- [Examples](examples) — Complete working examples
