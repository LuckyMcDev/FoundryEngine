package com.example

import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.bus.api.IEventBus
import com.example.dep.Dependency

class TestBundle extends BundleEntrypoint {
    TestBundle(IEventBus eventBus) {
        super(eventBus)
    }

    @Override
    void onLoad() {
        Dependency.hello()
        eventBus.addListener(ServerTickEvent.Post) { event ->
            println "tick tack toe!"
        }
    }
}