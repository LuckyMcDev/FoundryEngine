package com.example

import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder
import de.luckymcdev.foundryengine.api.event.BundleEvents
import de.luckymcdev.foundryengine.api.event.ClientEvents
import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.bundle.config.BundleConfigSpec
import de.luckymcdev.foundryengine.common.bundle.config.ConfigValue
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
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
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent
import net.minecraft.world.InteractionResult
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Items
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.item.component.ItemLore
import net.neoforged.bus.api.SubscribeEvent
import com.example.dep.Dependency

/**
 * This is the main entrypoint of the bundle.
 * This handles things like the event bus.
 *
 * Every bundle can have multiple of these!
 */
class TestBundle extends BundleEntrypoint {

    public static final String BUNDLEID = "testbundle"
    public static ConfigValue<Boolean> coolFeature
    public static ConfigValue<Integer> spawnRate

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

    /**
     * Constructor for the bundle entrypoint. You can do basic setup here, but you should avoid doing anything too complex until onLoad.
     */
    TestBundle(IEventBus eventBus, BundleConfig bundleConfig) {
        super(eventBus, bundleConfig)
    }

    /**
     * Helper method for creating identifiers with the bundle namespace.
     * You need to write this yourself.
     */
    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(BUNDLEID, path)
    }

    /**
     * Method called when the bundle is loaded.
     */
    @Override
    void onLoad() {
        BundleEvents.registry {
            it.items(THIS_IS_A_ITEM, ITEM_TWO, COSMIC_APPLE)
            it.blocks(MY_BLOCK)
            it.recipes(RECIPE)
            it.sounds(MY_SOUND)
        }

        ClientEvents.tick {
            if(Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendOverlayMessage(Component.literal("HELLO"))
            }
        }

        /*
        * Example of defining config values. These will be automatically synced to clients if changed on the server.
         */
        def spec = new BundleConfigSpec(bundleConfig)
        coolFeature = spec.defineBoolean("coolFeature", false, "Enables a cool feature.")
        spawnRate = spec.defineInt("spawnRate", 10, "Determines the spawn rate of something.")
        spec.build()
    }

    /**
     * Method called when the bundle is unloaded. You NEED to unregister any event handlers here.
     */
    @Override
    void onUnload() {
    }

}


