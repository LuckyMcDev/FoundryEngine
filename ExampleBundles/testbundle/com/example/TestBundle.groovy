package com.example

import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import io.github.luckymcdev.foundryengine.common.registry.builder.ItemBuilder
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
            event.register(Registries.ITEM) { registry ->
                ItemBuilder itemOne = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "this_is_a_item"))
                        .fireResistant()
                        .component(DataComponents.RARITY, Rarity.RARE)
                        .stacksTo(67)
                registry.register(itemOne.id, itemOne.build())


                List<Component> lines = List.of(
                        Component.literal("The legendary blade,"),
                        Component.literal("forged in the deeps.")
                )
                ItemBuilder itemTwo = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "item_two"))
                        .component(DataComponents.LORE, new ItemLore(lines))
                        .component(DataComponents.RARITY, Rarity.UNCOMMON)
                        .stacksTo(3)
                registry.register(itemTwo.id, itemTwo.build())


                FoodProperties cosmicFood = new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.3f)
                        .alwaysEdible()
                        .build()

                List<Component> appleLore = List.of(
                        Component.literal("A shimmering fruit from"),
                        Component.literal("another dimension.")
                )

                ItemBuilder itemThree = new ItemBuilder(Identifier.fromNamespaceAndPath(BUNDLEID, "cosmic_apple"))
                        .component(DataComponents.LORE, new ItemLore(appleLore))
                        .component(DataComponents.RARITY, Rarity.EPIC)
                        .component(DataComponents.FOOD, cosmicFood)
                        .component(DataComponents.CONSUMABLE, Consumables.defaultFood().build())
                        .stacksTo(16)

                registry.register(itemThree.id, itemThree.build())
            }
        }
    }
}