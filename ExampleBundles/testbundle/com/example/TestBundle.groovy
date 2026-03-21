package com.example

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.api.event.RegistryEvent
import de.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent
import de.luckymcdev.foundryengine.common.registry.EngineRegistries
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.criterion.InventoryChangeTrigger
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.util.datafix.fixes.FoodToConsumableFix
import net.minecraft.world.food.FoodConstants
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
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
    public static final String BUNDLEID = "testbundle"

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

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(BUNDLEID, path)
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
        private static final ItemBuilder THIS_IS_A_ITEM = ItemBuilder.create(id("this_is_a_item"))
                .fireResistant()
                .component(DataComponents.RARITY, Rarity.RARE)
                .stacksTo(67)

        private static final ItemBuilder ITEM_TWO = ItemBuilder.create(id("item_two"))
                .component(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("The legendary blade,"),
                        Component.literal("forged in the deeps.")
                )))
                .component(DataComponents.RARITY, Rarity.UNCOMMON)
                .stacksTo(3)

        private static final ItemBuilder COSMIC_APPLE = ItemBuilder.create(id("cosmic_apple"))
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

        private static final BlockBuilder MY_BLOCK = BlockBuilder.create(id("my_block"))
                .properties(p -> p.strength(2.0f, 3.0f))
                .itemProperties(p -> p.rarity(Rarity.COMMON))

        private static final RecipeBuilder RECIPE = RecipeBuilder.shapeless(id("custom_recipe"), Items.STICK)
                .requires(Items.DIAMOND, 1)
                .unlockedBy("has_log", InventoryChangeTrigger.TriggerInstance.hasItems(Items.OAK_LOG))

        @SubscribeEvent
        static void onRegister(RegistryEvent event) {
            event.blocks(MY_BLOCK)
            event.items(THIS_IS_A_ITEM, ITEM_TWO, COSMIC_APPLE)
            event.recipes(RECIPE)
        }
    }
}