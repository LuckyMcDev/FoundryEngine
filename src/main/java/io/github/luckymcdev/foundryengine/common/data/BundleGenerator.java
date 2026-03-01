package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.io.IOException;
import java.util.Locale;

public class BundleGenerator {
    private final Bundle bundle;
    private final DataGenerator dataGenerator;
    private final PackOutput pOut;

    public BundleGenerator(Bundle bundle) {
        this.bundle = bundle;
        this.dataGenerator = new DataGenerator(
                this.bundle.bundleFiles().root(),
                SharedConstants.getCurrentVersion(),
                true
        );
        this.pOut = dataGenerator.getPackOutput();
        addDefaults();
    }

    public void addDefaults() {
        addLang("en_us");
    }

    public void addLang(String locale) {
        addProvider(new LangGenerator(pOut, bundle, locale));
    }

    public void run() throws IOException {
        dataGenerator.run();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }

    private static class LangGenerator extends LanguageProvider {
        private final String id;
        private final Bundle bundle;

        public LangGenerator(PackOutput output, Bundle bundle, String locale) {
            super(output, bundle.info().id(), locale);
            this.id = bundle.info().id();
            this.bundle = bundle;
        }

        @Override
        protected void addTranslations() {
            BundleRegistryQuery query = bundle.registryQuery();

            query.getBlocks().forEach(block ->
                    add(block, formatTitleCase(BuiltInRegistries.BLOCK.getKey(block).getPath()))
            );

            query.getItems().forEach(item ->
                    add(item, formatTitleCase(BuiltInRegistries.ITEM.getKey(item).getPath()))
            );

            query.getEntityTypes().forEach(entityType ->
                    add(entityType, formatTitleCase(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath()))
            );

            query.getMobEffects().forEach(effect ->
                    add(effect, formatTitleCase(BuiltInRegistries.MOB_EFFECT.getKey(effect).getPath()))
            );

            query.getPotions().forEach(potion -> {
                String path = BuiltInRegistries.POTION.getKey(potion).getPath();
                add("item.minecraft.potion.effect." + path, "Potion of " + formatTitleCase(path));
                add("item.minecraft.splash_potion.effect." + path, "Splash Potion of " + formatTitleCase(path));
                add("item.minecraft.lingering_potion.effect." + path, "Lingering Potion of " + formatTitleCase(path));
                add("item.minecraft.tipped_arrow.effect." + path, "Arrow of " + formatTitleCase(path));
            });

            query.getFromRegistry(BuiltInRegistries.CREATIVE_MODE_TAB).forEach(tab -> {
                String path = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).getPath();
                add("itemGroup." + id + "." + path, formatTitleCase(path));
            });

            query.getAttributes().forEach(attr ->
                    add(attr.getDescriptionId(), formatTitleCase(BuiltInRegistries.ATTRIBUTE.getKey(attr).getPath()))
            );

            query.getFromRegistry(BuiltInRegistries.CUSTOM_STAT).forEach(stat -> {
                add("stat." + id + "." + stat.getPath(), formatTitleCase(stat.getPath()));
            });
        }

        private String formatTitleCase(String input) {
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
}
