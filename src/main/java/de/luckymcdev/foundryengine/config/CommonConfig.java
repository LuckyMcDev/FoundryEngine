package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig extends EngineConfig {
    static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DUMP_PACKS =
            BUILDER.comment("Whether to dump generated resource packs to the dump directory (debug).")
                    .define("DUMP_PACKS", false);

    public static final ModConfigSpec.ConfigValue<String> PACK_DUMP_DIRECTORY =
            BUILDER.comment("This is a config option for developers.",
                            "If you want to see the virtual resources of each bundle, they will be dumped here.",
                            "Only if DUMP is set to true")
                    .define("PACK_DUMP_DUMP_DIRECTORY", Common.TEMP_DIR.toString());

    public static final ModConfigSpec.BooleanValue FILE_NAME_HASH_COMPONENTS =
            BUILDER.comment("For the Icon Exporter, If components should be MD5-hashed in file names (and an auxiliary .txt file written with the full components string).")
                    .define("FILENAME_HASH_COMPONENTS", true);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }
}