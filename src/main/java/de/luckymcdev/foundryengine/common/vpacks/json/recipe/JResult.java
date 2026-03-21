package de.luckymcdev.foundryengine.common.vpacks.json.recipe;

import com.google.gson.JsonElement;
import de.luckymcdev.foundryengine.common.vpacks.json.PresetColor;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.*;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable.JConsumableComponent;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.variant.MobVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.entity.PotDecorations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JResult {
    private final Map<String, AbstractJComponent> components = new HashMap<>();
    private Identifier id;
    private int count;

    public JResult id(Identifier location) {
        this.id = location;
        return this;
    }

    public JResult count(int count) {
        this.count = count;
        return this;
    }

    // Allow inputting specific components.

    /**
     * Adds a data component to the result.
     * <br/>
     * {@link JResult} has numerous helper methods for adding specific components, check to see if
     * one is available before using this function.
     * <br/>
     * When adding non-vanilla components to {@link JResult}, it's advised that you use the
     * {@link #component(String, Object)} overload rather than extending {@link AbstractJComponent}
     * because that is an internal api, and is subject to change. Additionally, you should only use
     * the {@link #component(String, JsonElement)} overload if your non-vanilla component can't be
     * serialized without a type adapter.
     *
     * @param name  The name of the component
     * @param value The component
     * @return The current {@link JResult} instance
     */
    public JResult component(String name, AbstractJComponent value) {
        this.components.put(name, value);
        return this;
    }

    /**
     * Adds a data component to the result.
     * <br/>
     * <b>If you are attempting to add a vanilla component, refer to the {@link #component(String, AbstractJComponent)}
     * docs.</b>
     * <br/>
     * This is the goto method for adding non-vanilla components, however if your class would need
     * a type adapter, then you should serialize it to a {@link JsonElement} and pass it to the
     * {@link #component(String, JsonElement)} overload.
     *
     * @param name  The name of the component to add
     * @param value The directly-serializable value of the component
     * @param <T>   The directly-serializable type that the component uses
     * @return The current {@link JResult} instance
     */
    public <T> JResult component(String name, T value) {
        this.components.put(name, new JSimpleComponent<>(value));
        return this;
    }

    /**
     * Adds a data component to the result.
     * <br/>
     * This overload is meant to be used with non-vanilla components that won't
     * serialize without a type adapter. If that doesn't apply use {@link #component(String, AbstractJComponent)}
     * or {@link #component(String, Object)}
     *
     * @param name The name of the component
     * @param json The component represented/pre-serialized into json
     * @return The current {@link JResult} instance
     */
    public JResult component(String name, JsonElement json) {
        return this.component(name, new JSimpleComponent<>(json));
    }

    public JResult attributeModifiers(ItemAttributeModifiers modifiers) {
        return this.component("minecraft:attribute_modifiers", new JCodecComponent<>(modifiers, ItemAttributeModifiers.CODEC));
    }

    public JResult bannerPatterns(JBannerPatternsComponent component) {
        return this.component("minecraft:banner_patterns", component);
    }

    public JResult baseColor(JColorComponent component) {
        return this.component("minecraft:base_color", component);
    }

    public JResult bees(JBeesComponent component) {
        return this.component("minecraft:bees", component);
    }

    public JResult blockEntityData(Identifier resourceLocation) {
        return this.component("minecraft:block_entity_data", resourceLocation);
    }

    public JResult blockState(JStringMapComponent component) {
        return this.component("minecraft:block_state", component);
    }

    public JResult bucketEntityData(JBucketEntityDataComponent component) {
        return this.component("minecraft:bucket_entity_data", component);
    }

    public JResult bundleContents(JItemListComponent component) {
        return this.component("minecraft:bundle_contents", component);
    }

    public JResult canBreak(JPredicateListComponent component) {
        return this.component("minecraft:can_break", component);
    }

    public JResult canPlaceOn(JPredicateListComponent component) {
        return this.component("minecraft:can_place_on", component);
    }

    public JResult catCollar(PresetColor color) {
        return this.component("minecraft:cat/collar", new JColorComponent(color));
    }

    public JResult chargedProjectiles(JItemListComponent component) {
        return this.component("minecraft:charged_projectiles", component);
    }

    public JResult consumable(JConsumableComponent component) {
        return this.component("minecraft:consumable", component);
    }

    public JResult container(JContainerComponent component) {
        return this.component("minecraft:container", component);
    }

    public JResult containerLoot(JContainerLootComponent component) {
        return this.component("minecraft:container_loot", component);
    }

    public JResult creativeSlotLock(JMultitypeComponent component) {
        return this.component("minecraft:creative_slot_lock", component);
    }

    public JResult customData(JCustomDataComponent component) {
        return this.component("minecraft:custom_data", component);
    }

    public JResult customModelData(JCustomModelDataComponent component) {
        return this.component("minecraft:custom_model_data", component);
    }

    public JResult customName(String string) {
        return this.component("minecraft:custom_name", string);
    }

    public JResult damage(int damage) {
        return this.component("minecraft:damage", damage);
    }

    public JResult damageResistance(Identifier types) {
        return this.component("minecraft:damage_resistant", new JDamageResistanceComponent(types));
    }

    public JResult deathProtection(JDeathProtectionComponent component) {
        return this.component("minecraft:death_protection", component);
    }

    public JResult debugStickState(JStringMapComponent component) {
        return this.component("minecraft:debug_stick_state", component);
    }

    public JResult dyedColor(JDyedColorComponent component) {
        return this.component("minecraft:dyed_color", component);
    }

    public JResult enchantable(int value) {
        return this.component("minecraft:enchantable", value);
    }

    public JResult enchantmentGlintOverride(boolean override) {
        return this.component("minecraft:enchantment_glint_override", override);
    }

    public JResult enchantments(JEnchantmentsComponent component) {
        return this.component("minecraft:enchantments", component);
    }

    public JResult entityData(Identifier id) {
        return this.component("minecraft:entity_data", new JIdComponent(id));
    }

    public JResult equippable(Equippable equippableComponent) {
        return this.component("minecraft:equippable", new JCodecComponent<>(equippableComponent, Equippable.CODEC));
    }

    public JResult fireworkExplosion(JFireworkExplosionComponent component) {
        return this.component("minecraft:firework_explosion", component);
    }

    public JResult fireworks(JFireworksComponent component) {
        return this.component("minecraft:fireworks", component);
    }

    public JResult food(JFoodComponent component) {
        return this.component("minecraft:food", component);
    }

    public JResult glider() {
        return this.component("minecraft:glider", new EmptyJComponent());
    }

    public JResult instrument(Instrument instrument) {
        return this.component("minecraft:instrument", new JCodecComponent<>(instrument, Instrument.DIRECT_CODEC));
    }

    public JResult intangibleProjectile() {
        return this.component("minecraft:intangible_projectile", new EmptyJComponent());
    }

    public JResult itemModel(Identifier modelLocation) {
        return this.component("minecraft:item_model", modelLocation);
    }

    public JResult itemName(Component component) {
        return this.component("minecraft:item_name", new JCodecComponent<>(component, ComponentSerialization.CODEC));
    }

    public JResult jukeboxPlayable(Identifier resourceLocation) {
        return this.component("minecraft:jukebox_playable", resourceLocation);
    }

    public JResult lock(LockCode lockCode) {
        return this.component("minecraft:lock", new JCodecComponent<>(lockCode, LockCode.CODEC));
    }

    public JResult lodestoneTracker(LodestoneTracker lodestoneTracker) {
        return this.component("minecraft:lodestone_tracker", new JCodecComponent<>(lodestoneTracker, LodestoneTracker.CODEC));
    }

    public JResult lore(Component... components) {
        List<JCodecComponent<Component>> codecComponents = new ArrayList<>(components.length);
        for (int i = 0; i < components.length; i++) {
            codecComponents.add(i, new JCodecComponent<>(components[i], ComponentSerialization.CODEC));
        }
        return this.component("minecraft:lore", codecComponents);
    }

    public JResult mapColor(int mapColor) {
        return this.component("minecraft:map_color", mapColor);
    }

    public JResult mapDecorations(MapDecorations mapDecorations) {
        return this.component("minecraft:map_decorations", new JCodecComponent<>(mapDecorations, MapDecorations.CODEC));
    }

    public JResult mapId(int mapId) {
        return this.component("minecraft:map_id", mapId);
    }

    public JResult mapPostProcessing(MapPostProcessing mapPostProcessing) {
        return this.component("minecraft:map_post_processing", mapPostProcessing);
    }

    public JResult maxDamage(int maxDamage) {
        return this.component("minecraft:max_damage", maxDamage);
    }

    public JResult maxStackSize(int maxStackSize) {
        return this.component("minecraft:max_stack_size", maxStackSize);
    }

    public JResult noteBlockSound(Identifier noteBlockSound) {
        return this.component("minecraft:note_block_sound", noteBlockSound);
    }

    public JResult ominousBottleAmplifier(int amplifier) {
        return this.component("minecraft:ominous_bottle_amplifier", amplifier);
    }

    public JResult potDecorations(PotDecorations decorations) {
        return this.component("minecraft:pot_decorations", new JCodecComponent<>(decorations, PotDecorations.CODEC));
    }

    public JResult potionContents(PotionContents contents) {
        return this.component("minecraft:potion_contents", new JCodecComponent<>(contents, PotionContents.CODEC));
    }

    public JResult potionDurationScale(float durationScale) {
        return this.component("minecraft:potion_duration_scale", durationScale);
    }

    public JResult profile(ResolvableProfile profile) {
        return this.component("minecraft:profile", new JCodecComponent<>(profile, ResolvableProfile.CODEC));
    }

    public JResult providesBannerPatterns(Identifier tag) {
        return this.component("minecraft:provides_banner_pattern", "#" + tag);
    }

    public JResult providesTrimMaterial(ProvidesTrimMaterial material) {
        return this.component("minecraft:provides_trim_material", new JCodecComponent<>(material, ProvidesTrimMaterial.CODEC));
    }

    public JResult rarity(Rarity rarity) {
        return this.component("minecraft:rarity", new JCodecComponent<>(rarity, Rarity.CODEC));
    }

    public JResult recipes(JListComponent<Identifier> component) {
        return this.component("minecraft:recipes", component);
    }

    public JResult repairable(Repairable repairable) {
        return this.component("minecraft:repairable", new JCodecComponent<>(repairable, Repairable.CODEC));
    }

    public JResult repairCost(int cost) {
        return this.component("minecraft:repair_cost", cost);
    }

    public JResult storedEnchantments(JListComponent<Identifier> enchantments) {
        return this.component("minecraft:stored_enchantments", enchantments);
    }

    public JResult suspiciousStewEffects(JListComponent<JSuspiciousStewEffect> effects) {
        return this.component("minecraft:suspicious_stew_effects", effects);
    }

    public JResult tool(JToolComponent tool) {
        return this.component("minecraft:tool", tool);
    }

    public JResult tool(Tool tool) {
        return this.component("minecraft:tool", new JToolComponent(tool));
    }

    public JResult tooltip_style(Identifier style) {
        return this.component("minecraft:tooltip_style", style);
    }

    public JResult unbreakable() {
        return this.component("minecraft:unbreakable", new EmptyJComponent());
    }

    public JResult useCooldown(UseCooldown cooldown) {
        return this.component("minecraft:use_cooldown", new JCodecComponent<>(cooldown, UseCooldown.CODEC));
    }

    public JResult remainder(JMultitypeComponent component) {
        return this.component("minecraft:remainder", component);
    }

    public JResult weapon() {
        return this.component("minecraft:weapon", new EmptyJComponent());
    }

    public JResult weapon(Weapon weapon) {
        return this.component("minecraft:weapon", new JCodecComponent<>(weapon, Weapon.CODEC));
    }

    public JResult writableBookContent() {
        return this.component("minecraft:writable_book_content",
                new JCodecComponent<>(WritableBookContent.EMPTY, WritableBookContent.CODEC)
        );
    }

    public JResult writableBookContent(JWritableBookContentComponent pages) {
        return this.component("minecraft:writable_book_content", pages);
    }

    public JResult writtenBookContent() {
        return this.component("minecraft:written_book_content", new JCodecComponent<>(WrittenBookContent.EMPTY, WrittenBookContent.CODEC));
    }

    public JResult writtenBookContent(WrittenBookContent content) {
        return this.component("minecraft:written_book_content", new JCodecComponent<>(content, WrittenBookContent.CODEC));
    }

    public JResult writtenBookContent(JWrittenBookContentComponent content) {
        return this.component("minecraft:written_book_content", content);
    }

    public JResult mobVariant(MobVariant variant) {
        return this.component(variant.toString(), variant.value());
    }

    public JResult removedComponent(String name) {
        return this.component("!" + name, new EmptyJComponent());
    }
}
