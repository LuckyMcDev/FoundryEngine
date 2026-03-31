package com.example

import de.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import com.example.dep.Dependency
import com.example.post.TestPostProcessPipeline

/**
 * Handlers for in-game events (ticks, rendering, etc.)
 */
class TestBundleGameEvents {

    /**
     * Registers a custom post-processing pipeline to apply effects to the player's view.
     */
    @SubscribeEvent
    static void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
        event.register(new TestPostProcessPipeline())
    }

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