package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig {
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.ConfigValue<String> TEMP_DIRECTORY_INFO;
	public static final ModConfigSpec.ConfigValue<String> PACK_MODE;
	public static final ModConfigSpec.BooleanValue FILE_NAME_HASH_COMPONENTS;
	public static final ModConfigSpec.BooleanValue SCREEN_EFFECT_COMMAND_CHAINING;
	public static final ModConfigSpec.BooleanValue CUTSCENE_COMMAND_EXECUTION;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		TEMP_DIRECTORY_INFO = builder
			.comment("This is just info for where you temp directory is if you need it.")
			.translation("foundryengine.configuration.temp_directory_info")
			.define("TEMP_DIRECTORY_INFO", Common.TEMP_DIR.toString());

		PACK_MODE = builder
			.comment("The current pack mode. Set to \"dev\" (case-insensitive) to load bundle saves directly without instancing.")
			.translation("foundryengine.configuration.pack_mode")
			.define("PACK_MODE", "");

		FILE_NAME_HASH_COMPONENTS = builder
			.comment("For the Icon Exporter, If components should be MD5-hashed in file names (and an auxiliary .txt file written with the full components string).")
			.translation("foundryengine.configuration.filename_hash_components")
			.define("FILENAME_HASH_COMPONENTS", true);

		SCREEN_EFFECT_COMMAND_CHAINING = builder
			.comment("Allows the /engine screeneffect command to chain an arbitrary server command after the effect finishes. Disable to block command chaining.")
			.translation("foundryengine.configuration.screen_effect_command_chaining")
			.define("SCREEN_EFFECT_COMMAND_CHAINING", true);

		CUTSCENE_COMMAND_EXECUTION = builder
			.comment("Allows the editor cutscene command network packet to execute an arbitrary server command. Disable to block command execution from cutscenes.")
			.translation("foundryengine.configuration.cutscene_command_execution")
			.define("CUTSCENE_COMMAND_EXECUTION", true);

		SPEC = builder.build();
	}
}