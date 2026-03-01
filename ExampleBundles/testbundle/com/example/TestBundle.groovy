package com.example

import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.bus.api.IEventBus
import com.example.dep.Dependency
import com.example.post.TestPostProcessPipeline
import net.minecraft.network.chat.Component

/**
 * This File is a basic test for the Scripting.
 */
class TestBundle extends BundleEntrypoint {

    TestBundle(IEventBus eventBus) {
        super(eventBus)
    }

    @Override
    void onLoad() {
        eventBus.register(this)
    }

    @SubscribeEvent
    void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
        event.register(new TestPostProcessPipeline())
    }

    @SubscribeEvent
    void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().tickCount % 20 == 0) {
            event.getServer().getPlayerList().getPlayers().forEach { player ->

                //player.sendSystemMessage(Component.literal("Live editing. Also from the ingame editor now!"))
			

                Dependency.hello(player)
            }
        }
    }
}





