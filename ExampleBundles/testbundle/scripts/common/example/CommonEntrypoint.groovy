package common.example

import com.mojang.brigadier.CommandDispatcher
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.event.CommandEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.advancements.criterion.InventoryChangeTrigger
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionResult
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Items
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.item.component.ItemLore

class CommonEntrypoint implements BundleEntrypoint {

    public static final String BUNDLEID = "testbundle"

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

    // Recipe builders — one for each supported type
    private static final RecipeBuilder SHAPED_RECIPE = RecipeBuilder.shaped(id("test_shaped"), Items.DIAMOND_SWORD)
            .pattern(" D ", " D ", " S ")
            .define('D' as char, Items.DIAMOND)
            .define('S' as char, Items.STICK)
            .category(net.minecraft.data.recipes.RecipeCategory.COMBAT)
            .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))

    private static final RecipeBuilder SHAPELESS_RECIPE = RecipeBuilder.shapeless(id("test_shapeless"), Items.FLINT_AND_STEEL)
            .requires(Items.IRON_INGOT)
            .requires(Items.FLINT)
            .unlockedBy("has_iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))

    private static final RecipeBuilder SMELTING_RECIPE = RecipeBuilder.smelting(id("test_smelting"), Items.IRON_INGOT)
            .ingredient(Items.IRON_ORE)
            .experience(0.7f)
            .cookingTime(200)
            .unlockedBy("has_ore", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_ORE))

    private static final RecipeBuilder BLASTING_RECIPE = RecipeBuilder.blasting(id("test_blasting"), Items.IRON_INGOT)
            .ingredient(Items.IRON_ORE)
            .experience(0.7f)
            .cookingTime(100)
            .unlockedBy("has_ore", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_ORE))

    private static final RecipeBuilder SMOKING_RECIPE = RecipeBuilder.smoking(id("test_smoking"), Items.COOKED_BEEF)
            .ingredient(Items.BEEF)
            .experience(0.35f)
            .cookingTime(100)
            .unlockedBy("has_beef", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BEEF))

    private static final RecipeBuilder CAMPFIRE_RECIPE = RecipeBuilder.campfireCooking(id("test_campfire"), Items.COOKED_BEEF)
            .ingredient(Items.BEEF)
            .experience(0.35f)
            .cookingTime(600)
            .unlockedBy("has_beef", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BEEF))

    private static final RecipeBuilder STONECUTTING_RECIPE = RecipeBuilder.stonecutting(id("test_stonecutting"), Items.STONE_STAIRS)
            .ingredient(Items.STONE)
            .count(1)
            .unlockedBy("has_stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE))

    private static final RecipeBuilder SMITHING_TRANSFORM_RECIPE = RecipeBuilder.smithingTransform(id("test_smithing"), Items.NETHERITE_SWORD)
            .base(Items.DIAMOND_SWORD)
            .addition(Items.NETHERITE_INGOT)
            .template(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
            .unlockedBy("has_netherite", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))

    // Legacy shapeless recipe (kept for backward compatibility)
    private static final RecipeBuilder RECIPE = RecipeBuilder.shapeless(id("custom_recipe"), Items.STICK)
            .requires(Items.DIAMOND, 1)
            .unlockedBy("has_log", InventoryChangeTrigger.TriggerInstance.hasItems(Items.OAK_LOG))

    private static final SoundBuilder MY_SOUND = SoundBuilder.create(id("test"))

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(BUNDLEID, path)
    }

    @Override
    void onLoad() {
        BundleEvents.registry {
            it.items(THIS_IS_A_ITEM, ITEM_TWO, COSMIC_APPLE)
            it.blocks(MY_BLOCK)
            it.recipes(SHAPED_RECIPE, SHAPELESS_RECIPE, SMELTING_RECIPE, BLASTING_RECIPE,
                      SMOKING_RECIPE, CAMPFIRE_RECIPE, STONECUTTING_RECIPE, SMITHING_TRANSFORM_RECIPE,
                      RECIPE)
            it.sounds(MY_SOUND)
        }

    }

    @Override
    void onUnload() {
    }
}
