# Bundle Configuration

Bundle Configuration is currently messed up.

Bundles can define their own configuration with typed values, persisted as TOML files in `FoundryEngine/config/<bundleId>.toml`.

## Defining Configuration

In your entrypoint script, define a config spec:

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.bundle.config.BundleConfigSpec
import de.luckymcdev.foundryengine.common.bundle.config.ConfigValue
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint

class Entrypoint implements BundleEntrypoint {

    static ConfigValue<Integer> maxPlayers
    static ConfigValue<Boolean> enablePvP
    static ConfigValue<String> welcomeMessage

    @Override
    void onLoad() {
        def spec = new BundleConfigSpec(bundleConfig)
        maxPlayers = spec.defineInt("maxPlayers", 10,
            "Maximum number of players allowed")
        enablePvP = spec.defineBoolean("enablePvP", false,
            "Enable player vs player combat")
        welcomeMessage = spec.defineString("welcomeMessage", "Welcome!",
            "Message shown on join")
        spec.build()

        // Use the config values
        println "Max players: ${maxPlayers.get()}"
        println "PVP enabled: ${enablePvP.get()}"
        println "Welcome: ${welcomeMessage.get()}"
    }

    @Override
    void onUnload() {}
}
```

## ConfigValue Methods

| Method             | Description           |
|--------------------|-----------------------|
| `get()`            | Get the current value |
| `set(raw)`         | Set a new value       |
| `resetToDefault()` | Reset to default      |

## BundleConfigSpec Methods

| Method                                      | Description                             |
|---------------------------------------------|-----------------------------------------|
| `defineBoolean(key, defaultValue, comment)` | Define a boolean config value           |
| `defineInt(key, defaultValue, comment)`     | Define an integer config value          |
| `defineDouble(key, defaultValue, comment)`  | Define a double config value            |
| `defineString(key, defaultValue, comment)`  | Define a string config value            |
| `build()`                                   | Finalize spec and load values from disk |

## Generated TOML

The above spec produces a config file like:

```toml
# Maximum number of players allowed
#maxPlayers = 10
maxPlayers = 10

# Enable player vs player combat
#enablePvP = false
enablePvP = false

# Message shown on join
#welcomeMessage = "Welcome!"
welcomeMessage = "Welcome!"
```

## See Also

- [Entrypoints](entrypoint) — Bundle entrypoint scripts
- [Bundles](bundles) — Bundle manifest and structure
