package de.luckymcdev.foundryengine.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig extends EngineConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec.BooleanValue RENDER_OFFHAND =
            BUILDER.comment("If the offhand should be rendered the same way the main hand is being.")
                    .define("RENDER_OFFHAND", false);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }

    @Override
    public ModConfig.Type type() {
        return ModConfig.Type.CLIENT;
    }
}