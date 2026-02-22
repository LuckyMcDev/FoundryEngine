package com.example

import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.bus.api.IEventBus

class TestBundle extends BundleEntrypoint {
    TestBundle(IEventBus eventBus) {
        super(eventBus)
    }

    @Override
    void onLoad() {
        eventBus.addListener(ServerTickEvent.Post) { event ->
            println "tick tack toe!"
        }
    }
}