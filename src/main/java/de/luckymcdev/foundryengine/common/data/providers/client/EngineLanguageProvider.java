package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Locale;

public class EngineLanguageProvider extends LanguageProvider {
    private final String bundleId;

    public EngineLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.bundleId = modid;
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

    @Override
    protected void addTranslations() {
        Bundle bundle = Common.getBundleManager().getBundle(bundleId);
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
    }
}