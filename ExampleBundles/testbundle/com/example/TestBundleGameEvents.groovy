package com.example

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import com.example.dep.Dependency

/**
 * Handlers for in-game events (ticks, rendering, etc.)
 */
class TestBundleGameEvents {

    /**
     * Called every tick on the server side.
     * This example sends a message to all players every second, but you can do anything you want here.
     */
    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().tickCount % 20 == 0) {
            event.getServer().getPlayerList().getPlayers().forEach { player ->
                //player.sendSystemMessage(Component.literal("test"))
                Dependency.hello(player)
            }
        }
    }
}