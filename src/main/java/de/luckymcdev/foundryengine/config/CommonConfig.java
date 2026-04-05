package de.luckymcdev.foundryengine.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Path;

public final class CommonConfig extends EngineConfig {
    static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DUMP =
            BUILDER.comment("Whether to dump generated resource packs to the dump directory (debug).")
                    .define("dump", false);

    public static final ModConfigSpec.ConfigValue<String> DUMP_DIRECTORY =
            BUILDER.comment("Directory where resource pack dumps are written.")
                    .define("dumpDirectory", Path.of(SystemProperties.getProperty("java.io.tmpdir")).resolve("foundryengine").toString());

    public static final ModConfigSpec.BooleanValue FILE_NAME_HASH_COMPONENTS =
            BUILDER.comment("If components should be MD5-hashed in file names (and an auxiliary .txt file written with the full components string).")
                    .define("fileNameHashComponents", false);

    @Override
    public ModConfigSpec spec() {
        return BUILDER.build();
    }
}