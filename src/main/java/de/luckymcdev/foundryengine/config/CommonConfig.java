package de.luckymcdev.foundryengine.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig extends EngineConfig {
    static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DUMP = BUILDER
            .comment("Whether to dump generated resource packs to the dump directory (debug).")
            .define("dump", false);

    public static final ModConfigSpec.ConfigValue<String> DUMP_DIRECTORY = BUILDER
            .comment("Directory where resource pack dumps are written.")
            .define("dumpDirectory", System.getProperty("java.io.tmpdir") + "/foundryengine");

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }
}