package de.luckymcdev.foundryengine.bundle.common.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import de.luckymcdev.foundryengine.bundle.common.info.BundleInfo;
import de.luckymcdev.foundryengine.exceptions.common.UtilityClassException;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates TOML content from BundleInfo.
 */
public class BundleTomlWriter {
	private BundleTomlWriter() {
		throw new UtilityClassException();
	}

	public static String write(List<BundleInfo> bundles) {
		CommentedConfig config = TomlFormat.instance().createConfig();
		List<CommentedConfig> bundleList = new ArrayList<>();

		for (BundleInfo info : bundles) {
			CommentedConfig entry = TomlFormat.instance().createConfig();
			entry.set("bundleId", info.id());
			entry.set("displayName", info.displayName());
			entry.set("version", info.versionInfo().toString());
			entry.set("authors", String.join(", ", info.authors()));

			List<String> depStrings = info.dependencies().stream()
				.map(d -> d.type().name().toLowerCase() + ":" + d.id() + "@" + d.version())
				.toList();

			entry.set("dependencies", depStrings);
			bundleList.add(entry);
		}

		config.set("bundles", bundleList);
		return TomlFormat.instance().createWriter().writeToString(config);
	}
}