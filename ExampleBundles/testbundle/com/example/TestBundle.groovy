package com.example

import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import io.github.luckymcdev.foundryengine.common.registry.builder.ItemBuilder
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.bus.api.IEventBus
import com.example.dep.Dependency
import com.example.post.TestPostProcessPipeline
import net.neoforged.neoforge.registries.RegisterEvent

/**
 * Updated TestBundle with fixed event registration.
 */
class TestBundle extends BundleEntrypoint {
    private static final String BUNDLEID = "testbundle"

    TestBundle(IEventBus bundleBus, IEventBus eventBus) {
        super(bundleBus, eventBus)
    }

    @Override
    void onLoad() {
        eventBus.register(GameEvents)
        bundleBus.register(BundleEvents)
    }

    /**
     * Handlers for global game events (ServerTick, etc.)
     */
    static class GameEvents {

        @SubscribeEvent
        static void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
            event.register(new TestPostProcessPipeline())
        }

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

    /**
     * Handlers for bundle-specific events (Registration, etc.)
     */
    static class BundleEvents {
        @SubscribeEvent
        static void onRegister(RegisterEvent event) {
            event.register(Registries.ITEM, registry -> {
                ItemBuilder item = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "this_is_a_item"))
                        .fireResistant()
                        .component(DataComponents.RARITY, Rarity.EPIC)
                        .stacksTo(67)
                registry.register(item)
            })
        }
    }
}