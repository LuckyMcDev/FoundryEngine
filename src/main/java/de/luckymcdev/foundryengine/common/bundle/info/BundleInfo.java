package de.luckymcdev.foundryengine.common.bundle.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.List;

/**
 * Similar to {@link IModInfo}
 * <br>
 * All specified in "${bundleId}.bundles.toml"
 *
 * @param id          the id
 * @param displayName the displayname
 * @param authors     the authors
 * @param versionInfo the SemVer
 */
public record BundleInfo(String id, String displayName, List<String> authors, VersionInfo versionInfo) {
    public static final Codec<BundleInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(BundleInfo::id),
            Codec.STRING.fieldOf("display_name").forGetter(BundleInfo::displayName),
            Codec.STRING.listOf().fieldOf("authors").forGetter(BundleInfo::authors),
            VersionInfo.CODEC.fieldOf("version").forGetter(BundleInfo::versionInfo)
    ).apply(instance, BundleInfo::new));

    @Override
    public String toString() {
        return "{ " + id + ", " + displayName + ", " + authors + ", " + versionInfo + " }";
    }

    public record VersionInfo(int major, int minor, int patch) {
        public static final Codec<VersionInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("major").forGetter(VersionInfo::major),
                Codec.INT.fieldOf("minor").forGetter(VersionInfo::minor),
                Codec.INT.fieldOf("patch").forGetter(VersionInfo::patch)
        ).apply(instance, VersionInfo::new));

        public static final Codec<VersionInfo> STRING_CODEC = Codec.STRING.xmap(
                VersionInfo::parse,
                VersionInfo::toString
        );

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