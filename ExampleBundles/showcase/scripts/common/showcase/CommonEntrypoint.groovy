package showcase

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder
import de.luckymcdev.foundryengine.common.event.BlockEvents
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.event.PlayerEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.util.color.Color
import net.minecraft.advancements.criterion.InventoryChangeTrigger
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionResult
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Items
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.level.block.Blocks

class CommonEntrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path)
    }

    @Override
    void onLoad() {
        registerContent()
        registerEvents()
    }

    @Override
    void onUnload() {
        println "Showcase bundle unloaded!"
    }

    /** Registers all items, blocks, recipes, sounds, and particles. */
    private void registerContent() {
        def items = declareItems()
        def blocks = declareBlocks()
        def recipes = declareRecipes()
        def sound = declareSound()

        BundleEvents.registry {
            it.items(items)
            it.blocks(blocks)
            it.recipes(recipes)
            it.sounds(sound)
        }
    }

    /** Creates four showcase items: magic gem, wand, cosmic apple, and a healing heart clock. */
    private ItemBuilder[] declareItems() {
        def gem = ItemBuilder.create(id("magic_gem"))
            .component(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("A shimmering gem,"),
                Component.literal("pulled from the void.")
            )))
            .component(DataComponents.RARITY, Rarity.UNCOMMON)
            .stacksTo(16)

        def wand = ItemBuilder.create(id("wand"))
            .stacksTo(1)
            .use { level, player, hand ->
                player.sendSystemMessage(Component.literal("§eWhoosh! The wand glows brightly."))
                return InteractionResult.SUCCESS
            }

        def apple = ItemBuilder.create(id("cosmic_apple"))
            .component(DataComponents.RARITY, Rarity.EPIC)
            .component(DataComponents.FOOD, new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.5f).alwaysEdible().build())
            .component(DataComponents.CONSUMABLE, Consumables.defaultFood().build())
            .stacksTo(16)

        def clock = ItemBuilder.create(id("heart_clock"))
            .stacksTo(1)
            .inventoryTick { stack, level, owner, slot ->
                if (owner != null && owner.tickCount % 100 == 0) {
                    owner.heal(1.0f)
                }
            }

        return [gem, wand, apple, clock]
    }

    /** Creates three blocks: a glowing stone, a fire trap, and an invisible light source. */
    private BlockBuilder[] declareBlocks() {
        def glow = BlockBuilder.create(id("glowing_stone"))
            .properties { it.strength(3.0f, 6.0f).lightLevel { 15 } }

        def trap = BlockBuilder.create(id("fire_trap"))
            .properties { it.strength(2.0f, 3.0f) }
            .stepOn { level, pos, state, entity ->
                entity.setSecondsOnFire(3)
            }
            .destroy { level, pos, state ->
                println "Fire trap destroyed at $pos"
            }

        def light = BlockBuilder.create(id("invisible_light"))
            .noItem()
            .properties { it.noCollision().strength(-1.0f, 3600000.0f).lightLevel { 15 } }
            .generateData(false)

        return [glow, trap, light]
    }

    /** Creates a shaped, shapeless, and smelting recipe using vanilla items. */
    private RecipeBuilder[] declareRecipes() {
        def shaped = RecipeBuilder.shaped(id("wand_from_diamond"), Items.DIAMOND_HORSE_ARMOR)
            .pattern(" D ", " S ", " S ")
            .define('D' as char, Items.DIAMOND)
            .define('S' as char, Items.STICK)
            .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))

        def shapeless = RecipeBuilder.shapeless(id("gems_from_diamond"), Items.DIAMOND)
            .requires(Items.DIAMOND)
            .requires(Items.EMERALD)
            .count(3)
            .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))

        def smelting = RecipeBuilder.smelting(id("smelt_smooth"), Blocks.SMOOTH_STONE)
            .ingredient(Blocks.STONE)
            .experience(0.5f)
            .cookingTime(200)
            .unlockedBy("has_stone", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.STONE))

        return [shaped, shapeless, smelting]
    }

    /** Creates a magic chime sound. */
    private SoundBuilder declareSound() {
        return SoundBuilder.create(id("magic_chime"))
            .subtitle("Magic Chime")
            .addSound(id("magic_chime"))
            .range(16.0f)
    }

    /** Registers common event listeners. */
    private void registerEvents() {
        PlayerEvents.tick { event ->
            def player = event.entity
            if (player.tickCount % 100 == 0) {
                //player.sendSystemMessage(Component.literal("§7[Showcase] You have been playing for §e${player.tickCount / 20}§7 seconds!"))
            }
        }

        BlockEvents.broken { event ->
            println "[Showcase] ${event.player.name} broke ${event.state.block} at ${event.pos}"
        }
    }
}
