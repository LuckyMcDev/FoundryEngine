package de.luckymcdev.foundryengine.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig extends EngineConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue RENDER_OFFHAND =
            BUILDER.comment("If the offhand should be rendered the same way the main hand is being.")
                    .define("RENDER_OFFHAND", false);

    public static final ModConfigSpec.BooleanValue AUTO_EXPORT =
            BUILDER.comment("Automatically run the icon exporter on login when the registry has changed or no export exists yet.")
                    .define("autoExport", true);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }

    @Override
    public ModConfig.Type type() {
        return ModConfig.Type.CLIENT;
    }
}