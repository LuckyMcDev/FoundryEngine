package io.github.luckymcdev.foundryengine.common.bundle.info;

import java.util.List;

/**
 * Similar to {@link net.neoforged.neoforgespi.language.IModInfo}
 * <br>
 * All specified in "${bundleId}.bundles.toml"
 */
public class BundleInfo {

    private final String id;
    private final String displayName;
    private final List<String> authors;
    private final VersionInfo versionInfo;

    public BundleInfo(String id, String displayName, List<String> authors, VersionInfo versionInfo) {
        this.id = id;
        this.displayName = displayName;
        this.authors = authors;
        this.versionInfo = versionInfo;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public VersionInfo getBundleVersion() {
        return versionInfo;
    }

    @Override
    public String toString() {
        return "{ " + id + ", " + displayName + ", " + authors + ", " + versionInfo + " }";
    }

    public record VersionInfo(int major, int minor, int patch) {
        public static VersionInfo parse(String version) {
            String[] parts = version.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Version must be in format MAJOR.MINOR.PATCH, got: " + version);
            }
            return new VersionInfo(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}