package de.luckymcdev.foundryengine.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class StartupConfig extends EngineConfig {
    static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SCRIPTING_ENABLED = BUILDER
            .comment("Enables/disables loading of Groovy scripts from bundles.")
            .define("SCRIPTING_ENABLED", true);

    public static final ModConfigSpec.BooleanValue RESOURCES_ENABLED = BUILDER
            .comment("Enables/disables loading of resources from bundles.")
            .define("RESOURCES_ENABLED", true);

    public static final ModConfigSpec.BooleanValue EVAL_COMMAND_ENABLED = BUILDER
            .comment("Enables/disables the /engine eval command.")
            .define("EVAL_COMMAND_ENABLED", true);

    public static final ModConfigSpec.IntValue EVAL_COMMAND_PERMISSION = BUILDER
            .comment("Permission level required to use /engine eval (0–4).")
            .defineInRange("EVAL_COMMAND_PERMISSION", 4, 0, 4);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }

    @Override
    public ModConfig.Type type() {
        return ModConfig.Type.STARTUP;
    }
}