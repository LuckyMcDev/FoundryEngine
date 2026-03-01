package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.registry.BundleRegistry;
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
    private final String id;

    public BundleGenerator(Bundle bundle) {
        this.bundle = bundle;
        this.dataGenerator = new DataGenerator(
                this.bundle.bundleFiles().root(),
                SharedConstants.getCurrentVersion(),
                true
        );
        this.pOut = dataGenerator.getPackOutput();
        this.id = this.bundle.info().getId();
        addDefaults();
    }

    public void addDefaults() {
        addLang("en_us");
    }

    public void addLang(String locale) {
        addProvider(new LangGenerator(pOut, id, locale));
    }

    public void run() throws IOException {
        dataGenerator.run();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }

    private static class LangGenerator extends LanguageProvider {
        private final String id;

        public LangGenerator(PackOutput output, String modid, String locale) {
            super(output, modid, locale);
            id = modid;
        }

        @Override
        protected void addTranslations() {
            BundleRegistry registry = new BundleRegistry(id);

            registry.getBundleBlocks().forEach(block ->
                    add(block, formatTitleCase(BuiltInRegistries.BLOCK.getKey(block).getPath()))
            );

            registry.getBundleItems().forEach(item ->
                    add(item, formatTitleCase(BuiltInRegistries.ITEM.getKey(item).getPath()))
            );
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
