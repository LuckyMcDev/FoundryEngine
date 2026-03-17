package io.github.luckymcdev.foundryengine.common.vpacks.json;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JLang implements Cloneable {
    private final Map<String, String> lang = new HashMap<>();

    public static JLang lang() {
        return new JLang();
    }

    private <T> JLang addObject(Registry<T> registry, String string, T registryObject, String name) {
        return this.addObject(
                string,
                Objects.requireNonNull(registry.getKey(registryObject), "Your item should be registered before you call this method"),
                name);
    }

    private JLang addObject(String type, Identifier resourceLocation, String translation) {
        this.lang.put(type + "." + resourceLocation.getNamespace() + "." + resourceLocation.getPath(), translation);
        return this;
    }

    /**
     * Adds a translation entry, where the full translation key must be specified.
     *
     * @param entry The full translation key
     * @param name  The translation
     * @return The current {@link JLang} instance
     */
    public JLang addEntry(String entry, String name) {
        this.lang.put(entry, name);
        return this;
    }

    /**
     * Adds a translation for an item, and uses {@link Item#getDescriptionId()}
     * for the key.
     *
     * @param item The item used for the translation key
     * @param name The translation
     * @return The current {@link JLang} instance
     */
    public JLang addItemTranslation(Item item, String name) {
        this.lang.put(item.getDescriptionId(), name);
        return this;
    }

    /**
     * Adds a translation for an item, and uses {@link ItemStack#getItem()} &
     * {@link Item#getDescriptionId()}
     * for the key.
     *
     * @param itemStack The {@link ItemStack} used to get the translation key
     * @param name      The translation
     * @return The current {@link JLang} instance
     * @see JLang#addItemTranslation(ItemStack, String)
     */
    public JLang addItemTranslation(ItemStack itemStack, String name) {
        this.lang.put(itemStack.getItem().getDescriptionId(), name);
        return this;
    }

    /**
     * Adds a translation for an item, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the
     *                         key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addItemTranslation(Identifier resourceLocation, String name) {
        return this.addObject("item", resourceLocation, name);
    }

    /**
     * Adds a translation for a block, and uses {@link Block#getDescriptionId()}
     * for the key.
     *
     * @param block The {@link Block} used to get the translation key
     * @param name  The translation
     * @return The current {@link JLang} instance
     */
    public JLang addBlockTranslation(Block block, String name) {
        this.lang.put(block.getDescriptionId(), name);
        return this;
    }

    /**
     * Adds a translation for a block, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addBlockTranslation(Identifier resourceLocation, String name) {
        return this.addObject("block", resourceLocation, name);
    }

    /**
     * Adds a translation for a fluid.
     *
     * @param fluid The {@link Fluid} for which a translation is being added
     * @param name  The translation
     * @return The current {@link JLang} instance
     */
    public JLang addFluidTranslation(Fluid fluid, String name) {
        this.addObject(BuiltInRegistries.FLUID, "fluid", fluid, name);
        return this;
    }

    /**
     * Adds a translation for a fluid, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the
     *                         key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addFluidTranslation(Identifier resourceLocation, String name) {
        this.addObject("fluid", resourceLocation, name);
        return this;
    }

    /**
     * Adds a translation for an entity, and uses {@link EntityType#getDescriptionId()}
     * for the key.
     *
     * @param type The {@link EntityType} that is used to get the translation
     *             key
     * @param name The translation
     * @return The current {@link JLang} instance
     */
    public JLang addEntityTranslation(EntityType<?> type, String name) {
        this.lang.put(type.getDescriptionId(), name);
        return this;
    }

    /**
     * Adds a translation for an entity, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the
     *                         key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addEntityTranslation(Identifier resourceLocation, String name) {
        return this.addObject("entity_type", resourceLocation, name);
    }

    /**
     * <b>This does not do anything.</b>
     * Use {@link JLang#addEntry(String, String)} instead
     */
    // TODO: Implement addEnchantmentTranslation. 1.21 changed enchantments so
    //  their translation key, and now their is no direct way of getting their
    //  translation key.
    public JLang addEnchantmentTranslation(Enchantment enchantment, String name) {
        return this;
    }

    /**
     * Adds a translation for an item group, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the
     *                         key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addItemGroupTranslation(Identifier resourceLocation, String name) {
        return this.addObject("itemGroup", resourceLocation, name);
    }

    /**
     * Adds a translation for a sound, using the provided {@link Identifier}
     * to format the key.
     * <br />
     * For those who are unaware: sound translations are used for Minecraft's
     * subtitles.
     *
     * @param resourceLocation The {@link Identifier} used to format the
     *                         key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addSoundTranslation(Identifier resourceLocation, String name) {
        return this.addObject("sound_event", resourceLocation, name);
    }

    /**
     * Adds a translation for a status effect, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addStatusTranslation(Identifier resourceLocation, String name) {
        return this.addObject("mob_effect", resourceLocation, name);
    }

    /**
     * Adds a translation for a drinkable, splash, lingering potion, as well as
     * a tipped arrow.
     *
     * @param resourceLocation    The {@link Identifier} used to format the
     *                            key.
     * @param drinkablePotionName The translation for the drinkable potion
     * @param splashPotionName    The translation for the splash potion
     * @param lingeringPotionName The translation for the lingering potion
     * @param tippedArrowName     The translation for the tipped arrow
     * @return The current {@link JLang} instance
     */
    public JLang addAllPotionTranslations(
            Identifier resourceLocation,
            String drinkablePotionName,
            String splashPotionName,
            String lingeringPotionName,
            String tippedArrowName) {
        return this.addDrinkablePotionTranslation(resourceLocation, drinkablePotionName)
                .addSplashPotionTranslation(resourceLocation, splashPotionName)
                .addLingeringPotionTranslation(resourceLocation, lingeringPotionName)
                .addTippedArrowTranslation(resourceLocation, tippedArrowName);
    }

    /**
     * Adds a translation for a potion item, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addDrinkablePotionTranslation(Identifier resourceLocation, String name) {
        this.lang.put("item.minecraft.potion.effect." + resourceLocation.getPath(), name);
        return this;
    }

    /**
     * Adds a translation for a splash potion item, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addSplashPotionTranslation(Identifier resourceLocation, String name) {
        this.lang.put("item.minecraft.splash_potion.effect." + resourceLocation.getPath(), name);
        return this;
    }

    /**
     * Adds a translation for a lingering potion item, using the provided
     * {@link Identifier} to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addLingeringPotionTranslation(Identifier resourceLocation, String name) {
        this.lang.put("item.minecraft.lingering_potion.effect" + resourceLocation.getPath(), name);
        return this;
    }

    /**
     * Adds a translation for a tipped arrow item, using the provided
     * {@link Identifier} to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addTippedArrowTranslation(Identifier resourceLocation, String name) {
        this.lang.put("item.minecraft.tipped_arrow.effect." + resourceLocation.getPath(), name);
        return this;
    }

    /**
     * Adds a translation for a biome, using the provided {@link Identifier}
     * to format the key.
     *
     * @param resourceLocation The {@link Identifier} used to format the
     *                         key
     * @param name             The translation
     * @return The current {@link JLang} instance
     */
    public JLang addBiomeTranslation(Identifier resourceLocation, String name) {
        return this.addObject("biome", resourceLocation, name);
    }

    /**
     * @return Returns the language data in a {@link Map}, where the key is the
     * translation key and the value is the translation.
     */
    public Map<String, String> getLang() {
        return this.lang;
    }

    @Override
    public JLang clone() {
        try {
            return (JLang) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
