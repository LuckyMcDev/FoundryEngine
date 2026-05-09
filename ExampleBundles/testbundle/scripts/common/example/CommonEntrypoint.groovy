package common.example

import com.mojang.brigadier.CommandDispatcher
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder
import de.luckymcdev.foundryengine.api.event.AreaEvents
import de.luckymcdev.foundryengine.api.event.BundleEvents
import de.luckymcdev.foundryengine.api.event.CommandEvents
import de.luckymcdev.foundryengine.client.Client
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
            it.recipes(RECIPE)
            it.sounds(MY_SOUND)
        }

        AreaEvents.areaEnter {
            Client.getPlayer().sendSystemMessage(Component.literal("Entered Area: "+it.area.id()))
        }

        AreaEvents.areaLeave {
            Client.getPlayer().sendSystemMessage(Component.literal("Left Area: "+it.area.id()))
        }

        AreaEvents.areaTick {
            Client.getPlayer().sendSystemMessage(Component.literal("Tick Area: "+it.area.id()))
        }
    }

    @Override
    void onUnload() {
    }
}

