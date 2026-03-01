package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.io.IOException;

public class BundleGenerator {
    private final Bundle bundle;
    private final DataGenerator dataGenerator;

    public BundleGenerator(Bundle bundle) {
        this.bundle = bundle;
        this.dataGenerator = new DataGenerator(
                this.bundle.bundleFiles().root(),
                SharedConstants.getCurrentVersion(),
                true
        );
        addDefaults();
    }

    public void addDefaults() {
        addLang("en_us");
    }

    public void addLang(String locale) {
        addProvider(new LangGenerator(dataGenerator.getPackOutput(), bundle.info().getId(), locale));
    }

    public void run() throws IOException {
        dataGenerator.run();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }

    private static class LangGenerator extends LanguageProvider {
        public LangGenerator(PackOutput output, String modid, String locale) {
            super(output, modid, locale);
        }

        @Override
        protected void addTranslations() {
        }
    }
}
