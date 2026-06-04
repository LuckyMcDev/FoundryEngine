package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig extends EngineConfig {
    static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> TEMP_DIRECTORY_INFO =
            BUILDER.comment("This is just info for where you temp directory is if you need it.")
                    .define("DUMP_PACKS", Common.TEMP_DIR.toString());

    public static final ModConfigSpec.BooleanValue FILE_NAME_HASH_COMPONENTS =
            BUILDER.comment("For the Icon Exporter, If components should be MD5-hashed in file names (and an auxiliary .txt file written with the full components string).")
                    .define("FILENAME_HASH_COMPONENTS", true);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }
}