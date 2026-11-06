package de.luckymcdev.foundryengine.bundle.common.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A Maven dependency of a bundle.
 */
public record MavenLibrary(
	String group,
	String artifact,
	String version,
	@Nullable String sha256
) {

	public static final Codec<MavenLibrary> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("group").forGetter(MavenLibrary::group),
		Codec.STRING.fieldOf("artifact").forGetter(MavenLibrary::artifact),
		Codec.STRING.fieldOf("version").forGetter(MavenLibrary::version),
		Codec.STRING.optionalFieldOf("sha256").forGetter(lib -> Optional.ofNullable(lib.sha256()))
	).apply(instance, (group, artifact, version, sha256) ->
		new MavenLibrary(group, artifact, version, sha256.orElse(null))));

	/**
	 * Parses a compact coordinate: {@code group:artifact:version} or
	 * {@code group:artifact:version:sha256}.
	 *
	 * @throws IllegalArgumentException if the coordinate is malformed, or if the version is a
	 *                                  range / dynamic revision (those would require a real
	 *                                  Maven resolver)
	 */
	public static MavenLibrary parse(String coordinate) {
		String[] parts = coordinate.split(":", 4);
		if (parts.length < 3) {
			throw new IllegalArgumentException(
				"Invalid Maven library coordinate '" + coordinate
					+ "', expected group:artifact:version[:sha256]");
		}
		String group = parts[0].trim();
		String artifact = parts[1].trim();
		String version = parts[2].trim();
		String sha256 = parts.length > 3 && !parts[3].isBlank() ? parts[3].trim() : null;

		if (group.isEmpty() || artifact.isEmpty() || version.isEmpty()) {
			throw new IllegalArgumentException(
				"Invalid Maven library coordinate '" + coordinate
					+ "', group, artifact and version are required");
		}
		if (version.equalsIgnoreCase("LATEST") || version.equalsIgnoreCase("RELEASE")
			|| version.endsWith("+") || version.contains("[") || version.contains("(")) {
			throw new IllegalArgumentException(
				"Maven library version '" + version + "' must be exact; "
					+ "ranges and dynamic revisions are not supported");
		}
		return new MavenLibrary(group, artifact, version, sha256);
	}

	/**
	 * @return the jar's path relative to a Maven repository root, e.g.
	 * {@code org/jaudiotagger/jaudiotagger/3.0.1/jaudiotagger-3.0.1.jar}
	 */
	public String repositoryPath() {
		return group.replace('.', '/') + "/" + artifact + "/" + version + "/" + jarName();
	}

	public String jarName() {
		return artifact + "-" + version + ".jar";
	}

	@Override
	public String toString() {
		return sha256 != null
			? group + ":" + artifact + ":" + version + ":" + sha256
			: group + ":" + artifact + ":" + version;
	}
}