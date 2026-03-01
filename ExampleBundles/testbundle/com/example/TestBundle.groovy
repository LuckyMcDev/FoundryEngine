package com.example

import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import io.github.luckymcdev.foundryengine.common.Common
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.bus.api.IEventBus
import com.example.dep.Dependency
import com.example.post.TestPostProcessPipeline
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegisterEvent

/**
 * This File is a basic test for the Scripting.
 */
class TestBundle extends BundleEntrypoint {
    private static final String BUNDLEID = "testbundle"

    TestBundle(IEventBus bundleBus, IEventBus eventBus) {
        super(bundleBus, eventBus)
    }

    @Override
    void onLoad() {
        eventBus.register(this)
        bundleBus.addListener(RegisterEvent.class, this::onRegister)
    }

    void onRegister(RegisterEvent event) {
        event.register(
                Registries.ITEM,
                registry -> {
                    var id = Identifier.fromNamespaceAndPath(BUNDLEID, "testing_case_item")
                    var properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
                    registry.register(id, new Item(properties))
                }
        )
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