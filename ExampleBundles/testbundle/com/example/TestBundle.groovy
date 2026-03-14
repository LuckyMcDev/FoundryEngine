package com.example

import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import io.github.luckymcdev.foundryengine.common.registry.builder.ItemBuilder
import io.github.luckymcdev.foundryengine.common.registry.builder.BlockBuilder
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.util.datafix.fixes.FoodToConsumableFix
import net.minecraft.world.food.FoodConstants
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumable
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.item.component.ItemLore
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
        // 1. Register the static handlers in GameEvents to the global NeoForge event bus
        eventBus.register(GameEvents)

        // 2. Register the static handlers in BundleEvents to the specific Bundle bus
        // Since BundleEvents.onRegister is static, we register the class itself
        bundleBus.register(BundleEvents)
    }

    @Override
    void onUnload() {

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
        // Defining ItemBuilders as static constants
        private static final ItemBuilder THIS_IS_A_ITEM = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "this_is_a_item"))
                .fireResistant()
                .component(DataComponents.RARITY, Rarity.RARE)
                .stacksTo(67)

        private static final ItemBuilder ITEM_TWO = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "item_two"))
                .component(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("The legendary blade,"),
                        Component.literal("forged in the deeps.")
                )))
                .component(DataComponents.RARITY, Rarity.UNCOMMON)
                .stacksTo(3)

        private static final ItemBuilder COSMIC_APPLE = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "cosmic_apple"))
                .component(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("A shimmering fruit from"),
                        Component.literal("another dimension.")
                )))
                .component(DataComponents.RARITY, Rarity.EPIC)
                .component(DataComponents.FOOD, new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.3f)
                        .alwaysEdible()
                        .build())
                .component(DataComponents.CONSUMABLE, Consumables.defaultFood().build())
                .stacksTo(16)

        private static final BlockBuilder MY_BLOCK = new BlockBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "my_block"))
                .properties(p -> p.strength(2.0f, 3.0f))
                .itemProperties(p -> p.rarity(Rarity.COMMON))

        @SubscribeEvent
        static void onRegister(RegisterEvent event) {
            event.register(Registries.BLOCK, registry -> {
                MY_BLOCK.registerBlock(registry)
            })

            event.register(Registries.ITEM, registry -> {
                THIS_IS_A_ITEM.register(registry)
                ITEM_TWO.register(registry)
                COSMIC_APPLE.register(registry)
                MY_BLOCK.registerItem(registry)
            })
        }
    }
}