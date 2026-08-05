package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.common.util.CompatibilityMode;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class StartupConfig {
	public static final ModConfigSpec SPEC;
	public static final ModConfigSpec.ConfigValue<CompatibilityMode> COMPATIBILITY_MODE;
	public static final ModConfigSpec.BooleanValue SCRIPTING_ENABLED;
	public static final ModConfigSpec.BooleanValue EVAL_COMMAND_ENABLED;
	public static final ModConfigSpec.IntValue EVAL_COMMAND_PERMISSION;
	public static final ModConfigSpec.IntValue SCRIPT_TIMEOUT_SECONDS;
	public static final ModConfigSpec.BooleanValue CLEAR_DATA_CACHE;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		COMPATIBILITY_MODE = builder
			.comment("Sets the compatibility mode for FoundryEngine.")
			.comment("Compatibility means, what you support.")
			.comment("That means, when set to Client, you are client only,")
			.comment("when set to Server, you are server only,")
			.comment("and when set to Both, you are required on both client and server.")
			.translation("foundryengine.configuration.compatibility_mode")
			.defineEnum("COMPATIBILITY_MODE", CompatibilityMode.BOTH);

		SCRIPTING_ENABLED = builder
			.comment("Enables/disables loading of Groovy scripts from bundles.")
			.translation("foundryengine.configuration.scripting_enabled")
			.define("SCRIPTING_ENABLED", true);

		EVAL_COMMAND_ENABLED = builder
			.comment("Enables/disables the /engine eval command.")
			.translation("foundryengine.configuration.eval_command_enabled")
			.define("EVAL_COMMAND_ENABLED", true);

		EVAL_COMMAND_PERMISSION = builder
			.comment("Permission level required to use /engine eval (0–4).")
			.translation("foundryengine.configuration.eval_command_permission")
			.defineInRange("EVAL_COMMAND_PERMISSION", 4, 0, 4);

		SCRIPT_TIMEOUT_SECONDS = builder
			.comment("Maximum seconds a bundle script onLoad() or /engine eval may run before it is aborted and skipped.")
			.translation("foundryengine.configuration.script_timeout_seconds")
			.defineInRange("SCRIPT_TIMEOUT_SECONDS", 30, 1, 600);

		CLEAR_DATA_CACHE = builder
			.comment("If in the next run the data cache should be cleared.")
			.translation("foundryengine.configuration.clear_data_cache")
			.define("CLEAR_DATA_CACHE", false);

		SPEC = builder.build();
	}


}