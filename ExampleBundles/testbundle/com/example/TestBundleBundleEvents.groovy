package com.example

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.easing.Easing
import de.luckymcdev.foundryengine.common.util.color.Color
import net.minecraft.advancements.criterion.InventoryChangeTrigger
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.api.event.RegistryEvent
import net.minecraft.world.InteractionResult
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Items
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.item.component.ItemLore
import net.neoforged.bus.api.SubscribeEvent

import static com.example.TestBundle.id

/**
 * Handler for bundle-specific events (Registration, etc.)
 */
class TestBundleBundleEvents {
    private static final ItemBuilder THIS_IS_A_ITEM = ItemBuilder.create(id("this_is_a_item"))
            .fireResistant()
            .component("RARITY", Rarity.RARE)
            .stacksTo(67)
            .use((level, player, hand) -> {
                println("Test")
                return InteractionResult.PASS
            })

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

    /**
     * Event handler for the registration event. This is where you register blocks, items, recipes.
     * For more advanced registration see {@link RegisterEvent} provided by Neoforge.
     */
    @SubscribeEvent
    static void onRegister(RegistryEvent event) {
        event.blocks(
                MY_BLOCK
        )
        event.items(
                THIS_IS_A_ITEM,
                ITEM_TWO,
                COSMIC_APPLE
        )
        event.recipes(
                RECIPE
        )
    }
}