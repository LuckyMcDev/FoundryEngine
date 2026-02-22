package io.github.luckymcdev.foundryengine.common.bundle.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleInfo;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class BundleTomlParser {

    public static List<BundleInfo> parse(String tomlContent) {
        try (Reader reader = new StringReader(tomlContent)) {
            CommentedConfig config = TomlFormat.instance().createParser().parse(reader);
            return parseBundles(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bundle TOML", e);
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
        String authorsRaw = entry.getOrElse("authors", "");

        List<String> authors = Arrays.stream(authorsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return new BundleInfo(id, displayName, authors, BundleInfo.VersionInfo.parse(version));
    }

    private static String requireString(CommentedConfig entry, String key) {
        String value = entry.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Bundle TOML is missing required field: " + key);
        }
        return value;
    }
}