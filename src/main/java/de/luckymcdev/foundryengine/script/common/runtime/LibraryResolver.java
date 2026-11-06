package de.luckymcdev.foundryengine.script.common.runtime;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.bundle.common.info.MavenLibrary;
import de.luckymcdev.foundryengine.config.StartupConfig;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Downloads Maven libraries for a bundle into a local per-bundle cache.
 * <p>
 * Downloads are only attempted from repositories listed in
 * {@link StartupConfig#MAVEN_REPOSITORIES} (HTTPS only). If {@link MavenLibrary#sha256()} is
 * set, a downloaded jar is rejected unless its checksum matches; if
 * {@link StartupConfig#MAVEN_REQUIRE_SHA256} is enabled, libraries without a checksum are
 * rejected before any network call is made.
 * <p>
 * The resolver has no per-bundle state beyond the cache root, so one instance may be used
 * from multiple compile threads. Cache directories are per bundle, so two bundles declaring
 * the same coordinate download independent copies.
 */
public final class LibraryResolver {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(2);

	private final Path cacheRoot;
	private final HttpClient http;

	public LibraryResolver(Path cacheRoot) {
		this.cacheRoot = cacheRoot.toAbsolutePath().normalize();
		this.http = HttpClient.newBuilder()
			.connectTimeout(CONNECT_TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	}

	/**
	 * Rejects coordinates that would escape the cache or produce unsafe jar filenames.
	 * The codec path does not go through {@link MavenLibrary#parse}, so this is the single
	 * enforcement point for both TOML and in-memory input.
	 */
	private static void requireSafe(MavenLibrary library) throws IOException {
		requireSafeSegment(library.group(), "group", library);
		requireSafeSegment(library.artifact(), "artifact", library);
		requireSafeSegment(library.version(), "version", library);
	}

	private static void requireSafeSegment(String value, String field, MavenLibrary library) throws IOException {
		if (value.isEmpty() || value.equals(".") || value.equals("..")) {
			throw new IOException("Invalid " + field + " in library '" + library + "': '" + value + "'");
		}
		if (value.contains("..")) {
			throw new IOException("Invalid " + field + " in library '" + library + "': '" + value + "'");
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			boolean ok = Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '+' || c == '.';
			if (!ok) {
				throw new IOException("Invalid " + field + " in library '" + library + "': '" + value + "'");
			}
		}
	}

	/**
	 * Resolves {@code relative} under {@code base} and rejects anything that would escape it.
	 */
	private static Path safeResolve(Path base, String relative) throws IOException {
		Path candidate = base.resolve(relative).normalize();
		if (!candidate.startsWith(base)) {
			throw new IOException("Refusing library path outside cache root: " + relative);
		}
		return candidate;
	}

	private static boolean verifySha256(Path file, String expected) {
		try {
			return verifySha256(Files.readAllBytes(file), expected);
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean verifySha256(byte[] bytes, String expected) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] actual = digest.digest(bytes);
			byte[] expectedBytes = HexFormat.of().parseHex(expected.trim());
			return MessageDigest.isEqual(actual, expectedBytes);
		} catch (NoSuchAlgorithmException | IllegalArgumentException e) {
			LOGGER.warn("Unable to verify SHA-256 '{}': {}", expected, e.getMessage());
			return false;
		}
	}

	/**
	 * Resolves every jar for the given libraries, downloading from a whitelisted repository
	 * when not already cached.
	 *
	 * @throws IOException if any library cannot be resolved
	 */
	public List<Path> resolve(String bundleId, List<MavenLibrary> libraries) throws IOException {
		// Check if the feature is enabled globally
		if (libraries.isEmpty() || !StartupConfig.MAVEN_LIBRARIES_ENABLED.get()) {
			return List.of();
		}
		Path bundleCache = cacheRoot.resolve(bundleId);
		Files.createDirectories(bundleCache);

		List<Path> jars = new ArrayList<>(libraries.size());
		for (MavenLibrary library : libraries) {
			jars.add(resolveOne(bundleCache, library));
		}
		return List.copyOf(jars);
	}

	private Path resolveOne(Path bundleCache, MavenLibrary library) throws IOException {
		requireSafe(library);
		Path target = safeResolve(bundleCache, library.jarName());

		if (Files.isRegularFile(target)) {
			if (library.sha256() == null) {
				if (StartupConfig.MAVEN_REQUIRE_SHA256.get()) {
					throw new IOException("Cached library '" + library + "' has no SHA-256 in the bundle, "
						+ "and mavenLibraries.requireSha256 is enabled");
				}
				LOGGER.warn("Using cached library '{}' without checksum verification", library);
				return target;
			}
			if (verifySha256(target, library.sha256())) {
				return target;
			}
			LOGGER.warn("Cached jar '{}' failed checksum verification, deleting and re-downloading", library);
			Files.deleteIfExists(target);
		}

		if (library.sha256() == null && StartupConfig.MAVEN_REQUIRE_SHA256.get()) {
			throw new IOException("Library '" + library + "' has no SHA-256, and mavenLibraries.requireSha256 is enabled");
		}

		String relative = library.repositoryPath();
		IOException lastError = null;
		for (String repository : StartupConfig.MAVEN_REPOSITORIES.get()) {
			String base = repository.endsWith("/") ? repository : repository + "/";
			URI uri = URI.create(base + relative);
			try {
				byte[] body = download(uri);
				if (library.sha256() != null && !verifySha256(body, library.sha256())) {
					throw new IOException("SHA-256 mismatch for " + uri);
				}
				Files.write(target, body);
				LOGGER.info("Downloaded library '{}' from {}", library.jarName(), repository);
				return target;
			} catch (IOException e) {
				LOGGER.warn("Failed to download '{}' from {}: {}", library.jarName(), repository, e.getMessage());
				lastError = e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException("Interrupted while downloading " + uri, e);
			}
		}
		throw new IOException("Could not download library '" + library + "' from any whitelisted repository", lastError);
	}

	private byte[] download(URI uri) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(uri)
			.timeout(REQUEST_TIMEOUT)
			.GET()
			.build();
		HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
		if (response.statusCode() != 200) {
			throw new IOException("HTTP " + response.statusCode() + " from " + uri);
		}
		return response.body();
	}
}