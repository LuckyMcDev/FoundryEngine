# Concepts: Entrypoints

Entrypoints are the bridge between Foundry Engine and your Groovy scripts. Placing a
script in a scripts folder doesn't execute it — you need a class implementing
`BundleEntrypoint` to define what runs.

Entrypoints are loaded when the game starts and whenever you run `/engine reload`.
On reload, `onUnload()` is called for all entrypoints, then they are re-discovered
and `onLoad()` is called again.

## Basic Syntax

```groovy
package example

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint

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

## Per-Side Entrypoints

You can have separate entrypoints for each [side](sides.md):

| Location             | Purpose                                       |
|----------------------|-----------------------------------------------|
| `scripts/common/...` | Shared logic (items, blocks, recipes, events) |
| `scripts/client/...` | Client-only logic (rendering, keybinds, GUI)  |
| `scripts/server/...` | Server-only logic (commands, data)            |

Each side's entrypoint is loaded independently by the engine.

## See Also

- [Scripts](scripts.md) — Helper scripts vs entrypoint scripts
- [Sides](sides.md) — Client/server separation
- [Events](events.md) — Using events inside entrypoints