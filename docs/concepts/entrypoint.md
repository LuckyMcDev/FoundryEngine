# Concepts: Entrypoints

Entrypoints are Foundry Engines way to make something happen.
When you place a script in one of the folders, the engine finds it, but doesn't execute it.
You need do define an Entrypoint. Entrypoints are then loaded everytime you reload the engine via
`/engine reload`.

This is the basic syntax of an Entrypoint:

````groovy
package example

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
    }

    @Override
    void onUnload() {
    }
}
````