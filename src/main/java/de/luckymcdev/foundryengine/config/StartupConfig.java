package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.util.common.CompatibilityMode;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class StartupConfig {
	public static final ModConfigSpec SPEC;
	public static final ModConfigSpec.ConfigValue<CompatibilityMode> COMPATIBILITY_MODE;
	public static final ModConfigSpec.BooleanValue COMPATIBILITY_MODE_WARNING_SKIP;
	public static final ModConfigSpec.BooleanValue SCRIPTING_ENABLED;
	public static final ModConfigSpec.BooleanValue DISABLE_CLASS_FILTER;
	public static final ModConfigSpec.BooleanValue EVAL_COMMAND_ENABLED;
	public static final ModConfigSpec.IntValue EVAL_COMMAND_PERMISSION;
	public static final ModConfigSpec.IntValue SCRIPT_TIMEOUT_SECONDS;
	public static final ModConfigSpec.BooleanValue CLEAR_DATA_CACHE;
	public static final ModConfigSpec.BooleanValue MAVEN_LIBRARIES_ENABLED;
	public static final ModConfigSpec.BooleanValue MAVEN_REQUIRE_SHA256;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> MAVEN_REPOSITORIES;

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

		COMPATIBILITY_MODE_WARNING_SKIP = builder
			.comment("Enables / disables skipping the warning which appears when incompatible with the current compatibility mode.")
			.translation("foundryengine.configuration.compatibility_mode_warning_skip")
			.define("COMPATIBILITY_MODE_WARNING_SKIP", false);

		SCRIPTING_ENABLED = builder
			.comment("Enables/disables loading of Groovy scripts from bundles.")
			.translation("foundryengine.configuration.scripting_enabled")
			.define("SCRIPTING_ENABLED", true);

		DISABLE_CLASS_FILTER = builder
			.comment("Disables the blacklist filter on groovy scripts.")
			.comment("Requires approval by the user on first launch. If the user disagrees it will be set to false again.")
			.define("DISABLE_CLASS_FILTER", false);

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

		MAVEN_LIBRARIES_ENABLED = builder
			.comment("Allow bundles to declare and download Maven libraries. Disabling this refuses all library resolution.")
			.define("MAVEN_LIBRARIES_ENABLED", true);

		MAVEN_REQUIRE_SHA256 = builder
			.comment("Reject libraries whose .bundles.toml entry has no sha256. Strongly recommended for public servers.")
			.define("MAVEN_REQUIRE_SHA256", false);

		MAVEN_REPOSITORIES = builder
			.comment("Whitelisted Maven repositories, tried in order. Only https:// URLs are accepted.")
			.defineList(
				"MAVEN_REPOSITORIES",
				List.of("https://repo1.maven.org/maven2/"),
				o -> o instanceof String s && s.startsWith("https://"));

		SPEC = builder.build();
	}


}