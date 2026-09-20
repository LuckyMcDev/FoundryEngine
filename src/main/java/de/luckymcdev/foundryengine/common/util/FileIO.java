package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.common.Common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileIO {
	private static final long MAX_DOWNLOAD_BYTES = 256L * 1024 * 1024; // 256 MB
	private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(30);

	public static Path gameDir() {
		return Common.GAMEDIR;
	}

	public static Path resolve(String path) throws IOException {
		return Common.resolveAndValidate(Common.GAMEDIR.resolve(path));
	}

	public static String readText(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		return Files.readString(resolved, StandardCharsets.UTF_8);
	}

	public static List<String> readLines(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		return Files.readAllLines(resolved, StandardCharsets.UTF_8);
	}

	public static void writeText(Path path, String content) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		Files.createDirectories(resolved.getParent());
		Files.writeString(resolved, content, StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void appendText(Path path, String content) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		Files.createDirectories(resolved.getParent());
		Files.writeString(resolved, content, StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	public static byte[] readBytes(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		return Files.readAllBytes(resolved);
	}

	public static void writeBytes(Path path, byte[] bytes) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		Files.createDirectories(resolved.getParent());
		Files.write(resolved, bytes,
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void copy(Path source, Path target) throws IOException {
		Path resolvedSource = Common.resolveAndValidate(source);
		Path resolvedTarget = Common.resolveAndValidate(target);
		Files.createDirectories(resolvedTarget.getParent());
		Files.copy(resolvedSource, resolvedTarget, StandardCopyOption.REPLACE_EXISTING);
	}

	public static boolean delete(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		if (Files.isDirectory(resolved)) {
			throw new IOException("delete() does not accept directories: " + resolved);
		}
		return Files.deleteIfExists(resolved);
	}

	public static void deleteDirectory(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		if (!Files.isDirectory(resolved)) {
			throw new IOException("deleteDirectory() requires a directory: " + resolved);
		}
		try (var walk = Files.walk(resolved)) {
			var entries = walk.sorted(java.util.Comparator.reverseOrder()).toList();
			for (Path entry : entries) {
				Files.deleteIfExists(entry);
			}
		}
	}

	public static void mkdirs(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		Files.createDirectories(resolved);
	}

	public static List<Path> list(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		try (var stream = Files.list(resolved)) {
			return stream.toList();
		}
	}

	public static List<Path> listRecursive(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		try (var walk = Files.walk(resolved)) {
			return walk.filter(Files::isRegularFile).toList();
		}
	}

	public static boolean exists(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		return Files.isRegularFile(resolved);
	}

	public static boolean isDirectory(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		return Files.isDirectory(resolved);
	}

	public static FileMetadata metadata(Path path) throws IOException {
		Path resolved = Common.resolveAndValidate(path);
		BasicFileAttributes attrs = Files.readAttributes(resolved, BasicFileAttributes.class);
		return new FileMetadata(
			resolved.getFileName().toString(),
			attrs.size(),
			attrs.creationTime().toMillis(),
			attrs.lastModifiedTime().toMillis()
		);
	}

	public static void createZip(Path sourceDir, Path zipPath) throws IOException {
		Path resolvedSource = Common.resolveAndValidate(sourceDir);
		Path resolvedZip = Common.resolveAndValidate(zipPath);
		Files.createDirectories(resolvedZip.getParent());

		try (ZipOutputStream zos = new ZipOutputStream(
			Files.newOutputStream(resolvedZip, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			try (var walk = Files.walk(resolvedSource)) {
				for (Path file : walk.filter(Files::isRegularFile).toList()) {
					String entryName = resolvedSource.relativize(file).toString().replace('\\', '/');
					zos.putNextEntry(new ZipEntry(entryName));
					Files.copy(file, zos);
					zos.closeEntry();
				}
			}
		}
	}

	public static void createZipFromFiles(List<Path> files, Path zipPath) throws IOException {
		Path resolvedZip = Common.resolveAndValidate(zipPath);
		Files.createDirectories(resolvedZip.getParent());

		try (ZipOutputStream zos = new ZipOutputStream(
			Files.newOutputStream(resolvedZip, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			for (Path file : files) {
				Path resolved = Common.resolveAndValidate(file);
				zos.putNextEntry(new ZipEntry(resolved.getFileName().toString()));
				Files.copy(resolved, zos);
				zos.closeEntry();
			}
		}
	}

	public static List<String> listZip(Path zipPath) throws IOException {
		Path resolved = Common.resolveAndValidate(zipPath);
		List<String> entries = new ArrayList<>();
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(resolved))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				entries.add(entry.getName());
				zis.closeEntry();
			}
		}
		return entries;
	}

	public static String readZipEntry(Path zipPath, String entryName) throws IOException {
		Path resolved = Common.resolveAndValidate(zipPath);
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(resolved))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(entryName)) {
					return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
				}
				zis.closeEntry();
			}
		}
		throw new NoSuchElementException("Entry not found in zip: " + entryName);
	}

	public static byte[] readZipEntryBytes(Path zipPath, String entryName) throws IOException {
		Path resolved = Common.resolveAndValidate(zipPath);
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(resolved))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(entryName)) {
					return zis.readAllBytes();
				}
				zis.closeEntry();
			}
		}
		throw new NoSuchElementException("Entry not found in zip: " + entryName);
	}

	public static void extractZip(Path zipPath, Path destDir) throws IOException {
		Path resolvedZip = Common.resolveAndValidate(zipPath);
		Path resolvedDest = Common.resolveAndValidate(destDir);
		Files.createDirectories(resolvedDest);

		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(resolvedZip))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path entryPath = resolvedDest.resolve(entry.getName()).normalize();
				if (!entryPath.startsWith(resolvedDest)) {
					throw new IOException("Zip-slip detected for entry: " + entry.getName());
				}
				if (entry.isDirectory()) {
					Files.createDirectories(entryPath);
				} else {
					Files.createDirectories(entryPath.getParent());
					Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
				}
				zis.closeEntry();
			}
		}
	}

	public static void saveZipEntry(Path zipPath, String entryName, Path destPath) throws IOException {
		Path resolvedZip = Common.resolveAndValidate(zipPath);
		Path resolvedDest = Common.resolveAndValidate(destPath);
		Files.createDirectories(resolvedDest.getParent());

		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(resolvedZip))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(entryName)) {
					Files.copy(zis, resolvedDest, StandardCopyOption.REPLACE_EXISTING);
					return;
				}
				zis.closeEntry();
			}
		}
		throw new NoSuchElementException("Entry not found in zip: " + entryName);
	}

	public static void download(String url, Path destPath) throws IOException {
		Path resolved = Common.resolveAndValidate(destPath);
		Files.createDirectories(resolved.getParent());

		URI uri = URI.create(url);
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new IOException("Only http/https URLs are supported, got: " + scheme);
		}

		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(DOWNLOAD_TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri)
			.timeout(DOWNLOAD_TIMEOUT)
			.GET()
			.build();

		HttpResponse<InputStream> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Download interrupted: " + url, e);
		}

		int status = response.statusCode();
		if (status < 200 || status >= 300) {
			throw new IOException("Download failed with HTTP " + status + ": " + url);
		}

		try (InputStream body = response.body()) {
			byte[] buf = new byte[8192];
			long total = 0;
			try (var out = Files.newOutputStream(resolved,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				int n;
				while ((n = body.read(buf)) != -1) {
					total += n;
					if (total > MAX_DOWNLOAD_BYTES) {
						throw new IOException("Download exceeded size limit (" + MAX_DOWNLOAD_BYTES + " bytes): " + url);
					}
					out.write(buf, 0, n);
				}
			}
		}
	}

	public record FileMetadata(
		String name,
		long sizeBytes,
		long createdMs,
		long modifiedMs
	) {}
}