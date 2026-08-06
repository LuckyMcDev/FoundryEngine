# Groovy Scripts

A script is a `.groovy` file inside your bundle's `scripts/` folder. Scripts are how you tell FoundryEngine what to do.

## Two types of scripts

### 1. Entrypoint scripts

An entrypoint is the main file FoundryEngine runs when your bundle loads. It contains a class that implements `BundleEntrypoint`:

```groovy
package my_bundle

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        println "My bundle loaded!"
        // Register items, blocks, events here
    }

    @Override
    void onUnload() {
        println "My bundle unloaded!"
        // Clean up here
    }
}
```

**`onLoad()`** — runs when the bundle loads. This is where you register items, blocks, recipes, event listeners, and commands.

**`onUnload()`** — runs when the bundle is unloaded (e.g. `/engine reload`). Clean up any resources here.

Both of these methods **must** be implemented

### 2. Helper scripts

Any script that does NOT contain a `BundleEntrypoint` is a helper. Other scripts import it to use its methods:

```groovy
package my_bundle

static void sayHello(String name) {
    println "Hello, $name!"
}
```

```groovy
package my_bundle

import my_bundle.Utils

class Entrypoint implements BundleEntrypoint {
    @Override
    void onLoad() {
        Utils.sayHello("World")
    }
}
```

## Where scripts go

Scripts are organized by which side they run on:

```
scripts/
├── common/my_bundle/     # Runs on both client and server
├── client/my_bundle/     # Client-only (rendering, keybinds, GUI)
└── server/my_bundle/     # Server-only (commands, data)
```

The folder inside each side folder must match your `bundleId`. For example, if your `bundleId` is `my_bundle`, common scripts go in `scripts/common/my_bundle/`.

## What each side can do

| Folder    | What you can do                                                      |
|-----------|----------------------------------------------------------------------|
| `common/` | Register items, blocks, recipes, sounds, particles. Use most events. |
| `client/` | Rendering, GUI, key bindings, client ticks. Use `ClientEvents`.      |
| `server/` | Server commands, data management. Use `ServerEvents`.                |

Not every bundle needs all three folders. Only create the ones your bundle needs.

## Useful imports

Here are the most common imports you will use:

```groovy
import de.luckymcdev.foundryengine.common.Common                    // Access managers
import de.luckymcdev.foundryengine.common.event.BundleEvents         // Register content
import de.luckymcdev.foundryengine.common.event.*                    // All events
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder   // Create items
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder // Create blocks
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder // Create recipes
```

## Next

- [Client & Server](sides.md) — understanding the difference
- [Creating Items](creating-items.md) — make your first item
- [Events Guide](events-guide.md) — listen to game events
