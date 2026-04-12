package de.luckymcdev.foundryengine.common.vpacks;

import de.luckymcdev.foundryengine.api.builder.recipe.RecipeResult;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.vpacks.json.JLang;
import de.luckymcdev.foundryengine.common.vpacks.json.JSounds;
import de.luckymcdev.foundryengine.common.vpacks.json.model.JModel;
import de.luckymcdev.foundryengine.common.vpacks.json.model.JTextures;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JBlockModel;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JState;
import de.luckymcdev.foundryengine.common.vpacks.json.state.JVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
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
        sounds(pack, bundle);
        models(pack, bundle);
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

        for (CreativeModeTab tab : bundle.registryQuery().creativeModeTabs()) {
            Identifier id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            if (id != null) {
                lang.addItemGroupTranslation(id, formatTitleCase(bundle.info().id()));
            }
        }

        pack.addLang(bundle.id("en_us"), lang);
    }

    private static void sounds(VirtualResourcePack pack, Bundle bundle) {
        List<SoundEvent> soundEvents = bundle.registryQuery().getSoundEvents();
        if (soundEvents.isEmpty()) return;

        Map<String, JSounds> byNamespace = new LinkedHashMap<>();

        for (SoundEvent sound : soundEvents) {
            Identifier id = sound.location();
            JSounds jSounds = byNamespace.computeIfAbsent(id.getNamespace(), k -> JSounds.sounds());

            jSounds.add(sound, id);
        }

        byNamespace.forEach(pack::addSounds);
    }

    private static void models(VirtualResourcePack pack, Bundle bundle) {
        for (Block block : bundle.registryQuery().getBlocks()) {
            Identifier blockId = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block));
            String ns = blockId.getNamespace();
            String name = blockId.getPath();

            Identifier blockModelId = Identifier.fromNamespaceAndPath(ns, "block/" + name);
            pack.addModel(blockModelId, new JModel()
                    .parent("minecraft:block/cube_all")
                    .textures(new JTextures().add("all", ns + ":block/" + name)));

            pack.addBlockSate(blockId, new JState()
                    .add(new JVariant().empty().model(new JBlockModel(blockModelId))));

            Identifier itemModelId = Identifier.fromNamespaceAndPath(ns, "item/" + name);
            pack.addModel(itemModelId, new JModel().parent(blockModelId.toString()));

            pack.addItemDefinition(blockId, ns + ":item/" + name);
        }

        for (Item item : bundle.registryQuery().getItems()) {
            if (item instanceof BlockItem bi
                    && bundle.registryQuery().getBlocks().contains(bi.getBlock())) {
                continue;
            }

            Identifier itemId = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
            String ns = itemId.getNamespace();
            String name = itemId.getPath();

            Identifier itemModelId = Identifier.fromNamespaceAndPath(ns, "item/" + name);
            pack.addModel(itemModelId, new JModel()
                    .parent("minecraft:item/generated")
                    .textures(new JTextures().layer0(ns + ":item/" + name)));

            pack.addItemDefinition(itemId, ns + ":item/" + name);
        }
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

    /**
     * Formats a string like "example_block_name" to "Example Block Name".
     *
     * @param input the string to format.
     * @return the formatted string.
     */
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