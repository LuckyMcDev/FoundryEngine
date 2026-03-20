package io.github.luckymcdev.foundryengine.common.vpacks;

import io.github.luckymcdev.foundryengine.api.builder.recipe.RecipeResult;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.vpacks.json.JLang;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class BundleVirtualPacks {

    public static List<VirtualResourcePack> create() {
        AtomicInteger count = new AtomicInteger(0);
        List<VirtualResourcePack> virtualResourcePacks = new ArrayList<>();
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            virtualResourcePacks.add(createPack(bundle, count.incrementAndGet()));
        }
        return virtualResourcePacks;
    }

    private static VirtualResourcePack createPack(Bundle bundle, int count) {
        VirtualResourcePack pack = VirtualResourcePack.create(bundle.id("virtual_pack_" + count));
        lang(pack, bundle);
        recipes(pack, bundle);
        return pack;
    }

    private static void lang(VirtualResourcePack pack, Bundle bundle) {
        JLang lang = JLang.lang();

        for (Item item : bundle.registryQuery().getItems()) {
            addTranslations(item, BuiltInRegistries.ITEM, lang::addItemTranslation);
        }

        for (Block block : bundle.registryQuery().getBlocks()) {
            addTranslations(block, BuiltInRegistries.BLOCK, lang::addBlockTranslation);
        }

        for (Fluid fluid : bundle.registryQuery().getFluids()) {
            addTranslations(fluid, BuiltInRegistries.FLUID, lang::addFluidTranslation);
        }

        for (EntityType<?> entityType : bundle.registryQuery().getEntityTypes()) {
            addTranslations(entityType, BuiltInRegistries.ENTITY_TYPE, lang::addEntityTranslation);
        }

        for (SoundEvent sound : bundle.registryQuery().getSoundEvents()) {
            lang.addSoundTranslation(sound.location(), formatTitleCase(sound.location().getPath()));
        }

        for (Potion potion : bundle.registryQuery().getPotions()) {
            String name = formatTitleCase(BuiltInRegistries.POTION.getKey(potion).getPath());
            Identifier id = bundle.id(potion.name());
            lang.addAllPotionTranslations(id, name, name, name, name);
        }

        pack.addLang(bundle.id("en_us"), lang);
    }

    private static void recipes(VirtualResourcePack pack, Bundle bundle) {
        for (RecipeResult recipe : bundle.registryQuery().getRecipes()) {
            pack.addRecipe(recipe.id(), recipe.get());
        }
    }

    private static <T> void addTranslations(T object, Registry<T> registry, BiConsumer<T, String> translationAdder) {
        String path = Objects.requireNonNull(registry.getKey(object)).getPath();
        translationAdder.accept(object, formatTitleCase(path));
    }

    private static String formatTitleCase(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase(Locale.ENGLISH))
                        .append(" ");
            }
        }
        return builder.toString().trim();
    }
}
