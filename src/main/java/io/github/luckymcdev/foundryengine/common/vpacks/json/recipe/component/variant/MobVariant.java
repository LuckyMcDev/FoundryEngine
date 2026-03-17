package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.variant;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public record MobVariant(Identifier entity, String variantType, String value) {
    public MobVariant(String entity, String variantType, String value) {
        this(minecraftLocation(entity), variantType, value);
    }

    public MobVariant(String entity, String variantType, Identifier value) {
        this(minecraftLocation(entity), variantType, value.toString());
    }

    public static MobVariant axolotl(Axolotl.Variant variant) {
        return new MobVariant("axolotl", "variant", variant.getSerializedName());
    }

    public static MobVariant catCollar(DyeColor color) {
        return new MobVariant("cat", "collar", color.getSerializedName());
    }

    public static MobVariant cat(ResourceKey<@NotNull CatVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("cat", "variant", id);
    }

    public static MobVariant chicken(ResourceKey<@NotNull ChickenVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("chicken", "variant", id);
    }

    public static MobVariant cow(ResourceKey<@NotNull CowVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("cow", "variant", id);
    }

    public static MobVariant fox(Fox.Variant variant) {
        return new MobVariant("fox", "variant", variant.getSerializedName());
    }

    public static MobVariant frog(ResourceKey<@NotNull FrogVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("frog", "variant", id);
    }

    public static MobVariant horse(Variant variant) {
        return new MobVariant("horse", "variant", variant.getSerializedName());
    }

    public static MobVariant llama(Llama.Variant variant) {
        return new MobVariant("llama", "variant", variant.getSerializedName());
    }

    public static MobVariant mooshroom(MushroomCow.Variant variant) {
        return new MobVariant("mooshroom", "variant", variant.getSerializedName());
    }

    public static MobVariant painting(Identifier variant) {
        return new MobVariant("painting", "variant", variant);
    }

    public static MobVariant parrot(Parrot.Variant variant) {
        return new MobVariant("parrot", "variant", variant.getSerializedName());
    }

    public static MobVariant pig(ResourceKey<@NotNull PigVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("pig", "variant", id);
    }

    public static MobVariant rabbit(Rabbit.Variant variant) {
        return new MobVariant("rabbit", "variant", variant.getSerializedName());
    }

    public static MobVariant salmon(Salmon.Variant size) {
        return new MobVariant("salmon", "size", size.getSerializedName());
    }

    public static MobVariant sheep(DyeColor color) {
        return new MobVariant("sheep", "color", color.getSerializedName());
    }

    public static MobVariant shulkerColor(DyeColor color) {
        return new MobVariant("shulker", "color", color.getSerializedName());
    }

    public static MobVariant tropicalFishColor(DyeColor color) {
        return new MobVariant("tropical_fish", "base_color", color.getSerializedName());
    }

    public static MobVariant tropicalFishPattern(TropicalFish.Pattern pattern) {
        return new MobVariant("tropical_fish", "pattern", pattern.getSerializedName());
    }

    public static MobVariant tropicalFishPatternColor(DyeColor color) {
        return new MobVariant("tropical_fish", "pattern_color", color.getSerializedName());
    }

    public static MobVariant villager(ResourceKey<@NotNull VillagerType> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("villager", "variant", id);
    }

    public static MobVariant wolfCollar(DyeColor color) {
        return new MobVariant("wolf", "collar", color.getName());
    }

    public static MobVariant wolfSoundVariant(ResourceKey<@NotNull WolfSoundVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("wolf", "sound_variant", id);
    }

    public static MobVariant wolf(ResourceKey<@NotNull WolfVariant> variant) {
        //$ location_identifier
        Identifier id = variant.identifier();
        return new MobVariant("wolf", "variant", id);
    }

    private static Identifier minecraftLocation(String key) {
        return Identifier.withDefaultNamespace(key);
    }

    @Override
    public @NotNull String toString() {
        return this.entity.toString() + "/" + this.variantType;
    }
}
