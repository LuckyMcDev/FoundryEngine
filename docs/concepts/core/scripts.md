# Scripts & Entrypoints

A script is a `.groovy` file inside a bundle's `scripts/` folder. There are two types.

Scripts must be placed inside a **sub-package** matching your bundle namespace, not directly in the side folder. For example, a bundle with `bundleId = "my_bundle"` places its common scripts under `scripts/common/my_bundle/`.

## Entrypoint Scripts

An entrypoint script contains a class that implements `BundleEntrypoint`. It is loaded when the game starts and whenever `/engine reload` is called.

```groovy
package example

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.IEventBus

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        // Register listeners, builders, commands here
        BundleEvents.registry {
            it.items(myItem)
        }
    }

    @Override
    void onUnload() {
        // Clean up — called on /engine reload
    }
}
```

### `onLoad()`

Called during bundle loading. This is where you register event listeners, builders, commands, and config specs.

### `onUnload()`

Called when the bundle is unloaded (e.g. on `/engine reload`). Clean up any resources your bundle allocated.

## Helper Scripts

Any script that does **not** contain a `BundleEntrypoint` implementation is a helper script. It is never loaded automatically — other scripts must import it to use its methods.

```groovy
// example/Test.groovy
package example

void test(String argument) {
    println(argument)
}
```

```groovy
// example/Entrypoint.groovy
package example

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import example.Test

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        Test.test("Hello from Groovy!")
    }

    @Override
    void onUnload() {}
}
```

## Per-Side Scripts

Scripts are organized into three folders, each corresponding to a [side](sides). Inside each side folder, scripts must be in a sub-package matching your bundle namespace:

```
scripts/
├── common/my_bundle/      # Shared logic (items, blocks, recipes, events)
├── client/my_bundle/      # Client-only logic (rendering, keybinds, GUI)
└── server/my_bundle/      # Server-only logic (commands, data management)
```

| Location | Purpose |
|---|---|
| `scripts/common/<namespace>/` | Shared logic (items, blocks, recipes, events) |
| `scripts/client/<namespace>/` | Client-only logic (rendering, keybinds, GUI) |
| `scripts/server/<namespace>/` | Server-only logic (commands, data management) |

Each folder can have its own entrypoint. You don't need all three — only the sides your bundle requires.

## See also

- [Bundles](bundles) -- Bundle structure and lifecycle
- [Sides](sides) -- Client/server separation
- [Events](events) -- Using events inside entrypoints
