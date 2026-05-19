package server.example

import de.luckymcdev.foundryengine.api.event.ServerEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint

class ServerEntrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        ServerEvents.tick {
            if (it.server.tickCount % 20 == 0) {
                //it.server.sendSystemMessage(Component.literal("Test " + it.server.tickCount))
            }
        }
    }

    @Override
    void onUnload() {
    }
}