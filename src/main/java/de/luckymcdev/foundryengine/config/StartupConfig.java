package de.luckymcdev.foundryengine.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class StartupConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SCRIPTING_ENABLED;
    public static final ModConfigSpec.BooleanValue EVAL_COMMAND_ENABLED;
    public static final ModConfigSpec.IntValue EVAL_COMMAND_PERMISSION;
    public static final ModConfigSpec.BooleanValue CLEAR_DATA_CACHE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SCRIPTING_ENABLED = builder
                .comment("Enables/disables loading of Groovy scripts from bundles.")
                .define("SCRIPTING_ENABLED", true);

        EVAL_COMMAND_ENABLED = builder
                .comment("Enables/disables the /engine eval command.")
                .define("EVAL_COMMAND_ENABLED", true);

        EVAL_COMMAND_PERMISSION = builder
                .comment("Permission level required to use /engine eval (0–4).")
                .defineInRange("EVAL_COMMAND_PERMISSION", 4, 0, 4);

        CLEAR_DATA_CACHE = builder
                .comment("If in the next run the data cache should be cleared.")
                .define("CLEAR_DATA_CACHE", false);

        SPEC = builder.build();
    }
}