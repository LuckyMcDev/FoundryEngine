package de.luckymcdev.foundryengine.common.bundle.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import de.luckymcdev.foundryengine.common.bundle.info.BundleDependency;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * Parses the Toml for a bundle.
 */
public class BundleTomlParser {
	private BundleTomlParser() {
		throw new UtilityClassException();
	}

	public static List<BundleInfo> parse(String tomlContent) {
		try (Reader reader = new StringReader(tomlContent)) {
			CommentedConfig config = TomlFormat.instance().createParser().parse(reader);
			return parseBundles(config);
		} catch (Exception e) {
			throw new EngineException("Failed to parse bundle TOML", e);
		}
	}

	private static List<BundleInfo> parseBundles(CommentedConfig config) {
		List<CommentedConfig> bundleList = config.get("bundles");
		if (bundleList == null || bundleList.isEmpty()) {
			return List.of();
		}
		return bundleList.stream().map(BundleTomlParser::parseEntry).toList();
	}

	private static BundleInfo parseEntry(CommentedConfig entry) {
		String id = requireString(entry, "bundleId");
		String displayName = requireString(entry, "displayName");
		String version = requireString(entry, "version");

		List<String> depStrings = entry.getOrElse("dependencies", List.of());
		List<BundleDependency> dependencies = depStrings.stream()
			.map(BundleDependency::parse)
			.toList();

		List<String> authors = Arrays.stream((entry.getOrElse("authors", "")).split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();

		return new BundleInfo(id, displayName, authors, BundleInfo.VersionInfo.parse(version), dependencies);
	}

	private static String requireString(CommentedConfig entry, String key) {
		String value = entry.get(key);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Bundle TOML is missing required field: " + key);
		}
		return value;
	}
}