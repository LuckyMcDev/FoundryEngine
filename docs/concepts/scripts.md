# Concepts: Scripts

A Script is a file inside either the
`src/main/groovy/` or `bundleid/scripts/side`([sides](sides.md))
folder, with the `.groovy` file ending.

A Script can be of 2 types.

## Entrypoint Scripts

An Entrypoint script is a script, similar to this:

```groovy
package example

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.IEventBus

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
    }

    @Override
    void onUnload() {
    }
}
```

That means, the script file contains a class, which extends the BundleEntrypoint.

Every Entrypoint script is loaded when the game is started, and everytime the script
system is reloaded. That means, when you call /reload.

## Helper Scripts

Every Script, that is not an Entrypoint, is never loaded, unless called from another script.
That means, you need to import your script to then access methods in it.

For Example:

This is your Entrypoint:

```groovy
// example/Entrypoint.groovy
package example

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.IEventBus

class Entrypoint implements BundleEntrypoint {
    
    @Override
    void onLoad() {
    }

    @Override
    void onUnload() {
    }
}
```

And you have this script:

```groovy
// example/Test.groovy
package example

void test(String argument) {
    println(argument)
}

```

You can change your Entrypoint to this:

```groovy{7,13,14}
// example/Entrypoint.groovy
package example

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.IEventBus
import example.Test

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        //And can now in your code call this:
        Test.test("Hello")
    }

    @Override
    void onUnload() {
    }
}
```

And see a "Hello" in the Log Lines.
