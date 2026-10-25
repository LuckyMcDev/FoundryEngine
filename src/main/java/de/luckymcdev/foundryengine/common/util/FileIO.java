package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.common.Common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileIO {
	private static final long MAX_DOWNLOAD_BYTES = 256L * 1024 * 1024; // 256 MB
	private static final long MAX_EXTRACT_BYTES = 1024L * 1024 * 1024; // 1 GB per extraction call
	private static final int MAX_MEMORY_READ_BYTES = 64 * 1024 * 1024; // 64 MB for readZipEntry*
	private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(30);

	private static Path validate(Path path) throws IOException {
		return Common.resolveAndValidate(path);
	}

	private static void ensureParent(Path file) throws IOException {
		Path parent = file.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
	}

	/**
	 * Strips leading slashes and converts backslashes so entry names compare consistently.
	 */
	private static String normalizeEntryName(String name) {
		String n = name.replace('\\', '/');
		int i = 0;
		while (i < n.length() && n.charAt(i) == '/') {
			i++;
		}
		return n.substring(i);
	}

	/**
	 * Turns "level", "level/" or "" into a prefix usable with startsWith ("level/" or "").
	 */
	private static String toPrefix(String dirEntry) {
		String p = normalizeEntryName(dirEntry);
		return p.isEmpty() || p.endsWith("/") ? p : p + "/";
	}

	private static void copyLimited(InputStream in, Path target, ByteBudget budget) throws IOException {
		try (OutputStream out = Files.newOutputStream(target,
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) != -1) {
				budget.consume(n);
				out.write(buf, 0, n);
			}
		}
	}

	private static void moveReplacing(Path from, Path to) throws IOException {
		try {
			Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static Path gameDir() {
		return Common.GAMEDIR;
	}

	public static Path resolve(String path) throws IOException {
		return validate(Common.GAMEDIR.resolve(path));
	}

	public static String readText(Path path) throws IOException {
		return Files.readString(validate(path), StandardCharsets.UTF_8);
	}

	public static List<String> readLines(Path path) throws IOException {
		return Files.readAllLines(validate(path), StandardCharsets.UTF_8);
	}

	public static void writeText(Path path, String content) throws IOException {
		Path resolved = validate(path);
		ensureParent(resolved);
		Files.writeString(resolved, content, StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static void appendText(Path path, String content) throws IOException {
		Path resolved = validate(path);
		ensureParent(resolved);
		Files.writeString(resolved, content, StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	public static byte[] readBytes(Path path) throws IOException {
		return Files.readAllBytes(validate(path));
	}

	public static void writeBytes(Path path, byte[] bytes) throws IOException {
		Path resolved = validate(path);
		ensureParent(resolved);
		Files.write(resolved, bytes,
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Copies a file, or a whole directory tree. For directories the contents of
	 * {@code source} are merged into {@code target} (existing files are replaced).
	 * Symbolic links inside a copied tree are skipped.
	 */
	public static void copy(Path source, Path target) throws IOException {
		Path src = validate(source);
		Path dst = validate(target);
		if (Files.isDirectory(src)) {
			copyDirectory(src, dst);
		} else {
			ensureParent(dst);
			Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void copyDirectory(Path src, Path dst) throws IOException {
		if (dst.startsWith(src)) {
			throw new IOException("Cannot copy a directory into itself: " + src + " -> " + dst);
		}
		Files.walkFileTree(src, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(dst.resolve(src.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (attrs.isRegularFile()) { // attrs describe the link itself, so symlinks are skipped
					Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Deletes a file, or a directory including its contents.
	 * A symbolic link is removed without touching what it points to.
	 *
	 * @return true if something was deleted
	 */
	public static boolean delete(Path path) throws IOException {
		Path resolved = validate(path);
		if (Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS)) {
			deleteTree(resolved);
			return true;
		}
		return Files.deleteIfExists(resolved);
	}

	public static void deleteDirectory(Path path) throws IOException {
		Path resolved = validate(path);
		if (!Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException("deleteDirectory() requires a directory: " + resolved);
		}
		deleteTree(resolved);
	}

	private static void deleteTree(Path root) throws IOException {
		if (Files.isSameFile(root, Common.GAMEDIR)) {
			throw new IOException("Refusing to delete the game directory: " + root);
		}
		Files.walkFileTree(root, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc != null) {
					throw exc;
				}
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void mkdirs(Path path) throws IOException {
		Files.createDirectories(validate(path));
	}

	public static List<Path> list(Path path) throws IOException {
		try (var stream = Files.list(validate(path))) {
			return stream.toList();
		}
	}

	/**
	 * Lists all regular files below {@code path}.
	 */
	public static List<Path> listRecursive(Path path) throws IOException {
		try (var walk = Files.walk(validate(path))) {
			return walk.filter(Files::isRegularFile).toList();
		}
	}

	/**
	 * True if a file OR directory exists at {@code path}.
	 */
	public static boolean exists(Path path) throws IOException {
		return Files.exists(validate(path));
	}

	public static boolean isFile(Path path) throws IOException {
		return Files.isRegularFile(validate(path));
	}

	public static boolean isDirectory(Path path) throws IOException {
		return Files.isDirectory(validate(path));
	}

	public static FileMetadata metadata(Path path) throws IOException {
		Path resolved = validate(path);
		BasicFileAttributes attrs = Files.readAttributes(resolved, BasicFileAttributes.class);
		return new FileMetadata(
			resolved.getFileName().toString(),
			attrs.size(),
			attrs.creationTime().toMillis(),
			attrs.lastModifiedTime().toMillis()
		);
	}

	/**
	 * Zips a directory. Empty directories are kept; the zip itself is never included.
	 */
	public static void createZip(Path sourceDir, Path zipPath) throws IOException {
		Path src = validate(sourceDir);
		Path zip = validate(zipPath);
		if (!Files.isDirectory(src)) {
			throw new IOException("createZip() requires a directory: " + src);
		}
		ensureParent(zip);

		List<Path> paths;
		try (var walk = Files.walk(src)) {
			paths = walk.sorted().toList(); // parents sort before their children
		}

		try (ZipOutputStream zos = new ZipOutputStream(
			Files.newOutputStream(zip, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			for (Path p : paths) {
				if (p.equals(src) || p.equals(zip)) {
					continue;
				}
				String name = src.relativize(p).toString().replace('\\', '/');
				if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
					zos.putNextEntry(new ZipEntry(name + "/"));
					zos.closeEntry();
				} else if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
					zos.putNextEntry(new ZipEntry(name));
					Files.copy(p, zos);
					zos.closeEntry();
				}
			}
		}
	}

	/**
	 * Zips individual files flat (by file name). Two files with the same name are rejected.
	 */
	public static void createZipFromFiles(List<Path> files, Path zipPath) throws IOException {
		Path zip = validate(zipPath);
		ensureParent(zip);

		try (ZipOutputStream zos = new ZipOutputStream(
			Files.newOutputStream(zip, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			Set<String> used = new HashSet<>();
			for (Path file : files) {
				Path resolved = validate(file);
				String name = resolved.getFileName().toString();
				if (!used.add(name)) {
					throw new IOException("Duplicate file name in zip: " + name);
				}
				zos.putNextEntry(new ZipEntry(name));
				Files.copy(resolved, zos);
				zos.closeEntry();
			}
		}
	}

	private static ZipFile openZip(Path resolvedZip) throws IOException {
		return new ZipFile(resolvedZip.toFile());
	}

	public static List<String> listZip(Path zipPath) throws IOException {
		try (ZipFile zf = openZip(validate(zipPath))) {
			return new ArrayList<>(zf.stream().map(ZipEntry::getName).toList());
		}
	}

	public static String readZipEntry(Path zipPath, String entryName) throws IOException {
		return new String(readZipEntryBytes(zipPath, entryName), StandardCharsets.UTF_8);
	}

	public static byte[] readZipEntryBytes(Path zipPath, String entryName) throws IOException {
		try (ZipFile zf = openZip(validate(zipPath))) {
			ZipEntry entry = requireFileEntry(zf, entryName);
			try (InputStream in = zf.getInputStream(entry)) {
				byte[] data = in.readNBytes(MAX_MEMORY_READ_BYTES + 1);
				if (data.length > MAX_MEMORY_READ_BYTES) {
					throw new IOException("Zip entry too large to read into memory (limit "
						+ MAX_MEMORY_READ_BYTES + " bytes): " + entryName);
				}
				return data;
			}
		}
	}

	private static ZipEntry requireFileEntry(ZipFile zf, String entryName) throws IOException {
		String name = normalizeEntryName(entryName);
		ZipEntry entry = zf.getEntry(name);
		if (entry == null) {
			entry = zf.stream()
				.filter(e -> normalizeEntryName(e.getName()).equals(name))
				.findFirst()
				.orElse(null);
		}
		if (entry == null) {
			String prefix = name.endsWith("/") ? name : name + "/";
			if (zf.stream().anyMatch(e -> normalizeEntryName(e.getName()).startsWith(prefix))) {
				throw new IOException("Entry is a directory, use extractZipDirectory(): " + entryName);
			}
			throw new NoSuchElementException("Entry not found in zip: " + entryName);
		}
		if (entry.isDirectory()) {
			throw new IOException("Entry is a directory, use extractZipDirectory(): " + entryName);
		}
		return entry;
	}

	/**
	 * A name counts as a directory if it is empty, ends with '/', or any entry lives below it.
	 * Many zip tools never write explicit directory entries, so the entry itself may not exist.
	 */
	private static boolean isZipDirectory(ZipFile zf, String name) {
		if (name.isEmpty() || name.endsWith("/")) {
			return true;
		}
		String prefix = name + "/";
		return zf.stream().anyMatch(e -> normalizeEntryName(e.getName()).startsWith(prefix));
	}

	public static void extractZip(Path zipPath, Path destDir) throws IOException {
		extractZipDirectory(zipPath, "", destDir);
	}

	/**
	 * Extracts everything below {@code dirEntry} (e.g. "level/") into {@code destDir},
	 * with the {@code dirEntry} prefix stripped. An empty {@code dirEntry} extracts the whole zip.
	 */
	public static void extractZipDirectory(Path zipPath, String dirEntry, Path destDir) throws IOException {
		Path resolvedZip = validate(zipPath);
		Path resolvedDest = validate(destDir);
		try (ZipFile zf = openZip(resolvedZip)) {
			extractUnder(zf, toPrefix(dirEntry), resolvedDest);
		}
	}

	/**
	 * Extracts a single entry to {@code destPath}. If {@code entryName} refers to a
	 * directory ("level" or "level/"), its contents are extracted into {@code destPath}.
	 */
	public static void saveZipEntry(Path zipPath, String entryName, Path destPath) throws IOException {
		Path resolvedZip = validate(zipPath);
		Path resolvedDest = validate(destPath);
		String name = normalizeEntryName(entryName);

		try (ZipFile zf = openZip(resolvedZip)) {
			if (isZipDirectory(zf, name)) {
				extractUnder(zf, toPrefix(name), resolvedDest);
			} else {
				ZipEntry entry = requireFileEntry(zf, name);
				ensureParent(resolvedDest);
				try (InputStream in = zf.getInputStream(entry)) {
					copyLimited(in, resolvedDest, new ByteBudget("Extraction", MAX_EXTRACT_BYTES));
				}
			}
		}
	}

	private static void extractUnder(ZipFile zf, String prefix, Path destDir) throws IOException {
		List<? extends ZipEntry> matches = zf.stream()
			.filter(e -> normalizeEntryName(e.getName()).startsWith(prefix))
			.toList();
		if (matches.isEmpty() && !prefix.isEmpty()) {
			throw new NoSuchElementException("Directory not found in zip: " + prefix);
		}

		Path root = destDir.toAbsolutePath().normalize();
		Files.createDirectories(root);
		ByteBudget budget = new ByteBudget("Extraction", MAX_EXTRACT_BYTES);

		for (ZipEntry entry : matches) {
			String name = normalizeEntryName(entry.getName());
			String relative = name.substring(prefix.length());
			if (relative.isEmpty()) {
				continue; // the directory entry itself
			}

			Path target = root.resolve(relative).normalize();
			if (!target.startsWith(root)) {
				throw new IOException("Zip-slip detected for entry: " + entry.getName());
			}

			if (name.endsWith("/")) {
				Files.createDirectories(target);
			} else {
				if (Files.isDirectory(target)) {
					throw new IOException("Zip entry conflicts with existing directory: " + entry.getName());
				}
				ensureParent(target);
				try (InputStream in = zf.getInputStream(entry)) {
					copyLimited(in, target, budget);
				}
			}
		}
	}

	/**
	 * Downloads to a temp file next to {@code destPath} and moves it into place on success,
	 * so a failed download never leaves a partial file or destroys an existing one.
	 */
	public static void download(String url, Path destPath) throws IOException {
		Path resolved = validate(destPath);

		URI uri;
		try {
			uri = URI.create(url);
		} catch (IllegalArgumentException e) {
			throw new IOException("Invalid URL: " + url, e);
		}
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new IOException("Only http/https URLs are supported, got: " + scheme);
		}

		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri)
			.timeout(DOWNLOAD_TIMEOUT)
			.GET()
			.build();

		HttpResponse<InputStream> response;
		try {
			response = Http.CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Download interrupted: " + url, e);
		}

		// body is closed on every path, including non-2xx responses
		try (InputStream body = response.body()) {
			int status = response.statusCode();
			if (status < 200 || status >= 300) {
				throw new IOException("Download failed with HTTP " + status + ": " + url);
			}

			ensureParent(resolved);
			Path tmp = Files.createTempFile(resolved.toAbsolutePath().getParent(),
				resolved.getFileName() + ".", ".part");
			try {
				copyLimited(body, tmp, new ByteBudget("Download", MAX_DOWNLOAD_BYTES));
				moveReplacing(tmp, resolved);
			} finally {
				Files.deleteIfExists(tmp);
			}
		}
	}

	private static final class ByteBudget {
		private final String label;
		private final long limit;
		private long used;

		ByteBudget(String label, long limit) {
			this.label = label;
			this.limit = limit;
		}

		void consume(int n) throws IOException {
			used += n;
			if (used > limit) {
				throw new IOException(label + " exceeded size limit (" + limit + " bytes)");
			}
		}
	}

	private static final class Http {
		static final HttpClient CLIENT = HttpClient.newBuilder()
			.connectTimeout(DOWNLOAD_TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
	}

	public record FileMetadata(
		String name,
		long sizeBytes,
		long createdMs,
		long modifiedMs
	) {
	}
}