package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Locale;

public class EngineLanguageProvider extends LanguageProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineLanguageProvider(PackOutput output, String locale, Bundle bundle) {
        super(output, bundle.info().id(), locale);
        this.bundle = bundle;
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

    @Override
    public Bundle bundle() {
        return bundle;
    }

    @Override
    protected void addTranslations() {
        BundleRegistryQuery query = bundle.registryQuery();

        query.getBlocks().forEach(block -> {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            add(block, formatTitleCase(id.getPath()));
        });

        query.getItems().forEach(item -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            add(item, formatTitleCase(id.getPath()));
        });

        query.getEntityTypes().forEach(entityType -> {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            add(entityType, formatTitleCase(id.getPath()));
        });

        query.getMobEffects().forEach(effect -> {
            Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            add(effect, formatTitleCase(id.getPath()));
        });

        query.creativeModeTabs().forEach(tab -> {
            Identifier id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            add("itemGroup." + id.getNamespace() + "." + id.getPath(), formatTitleCase(id.getPath()));
        });

        query.getFluids().forEach(fluid -> {
            Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
            add("fluid." + id.getNamespace() + "." + id.getPath(), formatTitleCase(id.getPath()));
        });

        query.getPotions().forEach(potion -> {
            Identifier id = BuiltInRegistries.POTION.getKey(potion);
            String name = formatTitleCase(id.getPath());
            add("item.minecraft.potion.effect." + id.getPath(), name);
            add("item.minecraft.splash_potion.effect." + id.getPath(), name);
            add("item.minecraft.lingering_potion.effect." + id.getPath(), name);
            add("item.minecraft.tipped_arrow.effect." + id.getPath(), name);
        });

        query.getSoundEvents().forEach(sound -> {
            Identifier id = sound.location();
            add("sound_event." + id.getNamespace() + "." + id.getPath(), formatTitleCase(id.getPath()));
        });
    }
}
