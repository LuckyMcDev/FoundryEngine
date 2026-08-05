package de.luckymcdev.foundryengine.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.data.gen.BundleDataGenerator;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackMerger {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static Path mergeToZip(Path outputZip) {
		Path staging = Common.TEMP_DIR.resolve("resourcepack-merge");
		try {
			FileUtils.deleteDirectory(staging.toFile());
			Files.createDirectories(staging);
			mergeAssetsInto(staging);
			writePackMeta(staging);
			zipDirectory(staging, outputZip);
		} catch (IOException e) {
			LOGGER.error("[FoundryEngine] Failed to merge resource packs into '{}': {}", outputZip, e.getMessage());
		} finally {
			FileUtils.deleteQuietly(staging.toFile());
		}
		return outputZip;
	}

	private static void mergeAssetsInto(Path staging) throws IOException {
		List<Path> sources = orderByPriority();
		for (Path source : sources) {
			if (!Files.isDirectory(source)) {
				continue;
			}
			Path assetsDir = staging.resolve("assets");
			Files.createDirectories(assetsDir);
			try (var stream = Files.walk(source)) {
				for (Path file : stream.filter(Files::isRegularFile).toList()) {
					Path relative = source.relativize(file);
					Path target = assetsDir.resolve(relative.toString());
					Files.createDirectories(target.getParent());
					Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	/**
	 * Generated assets first, then bundles, so later sources overwrite earlier ones.
	 */
	private static List<Path> orderByPriority() {
		List<Path> paths = new ArrayList<>();
		paths.add(BundleDataGenerator.getGeneratedAssetsPath());
		Common.getBundleManager().getBundles().forEach(b -> paths.add(b.bundleFiles().assets()));
		return paths;
	}

	private static void writePackMeta(Path staging) throws IOException {
		JsonObject pack = new JsonObject();
		pack.addProperty("description", "Foundry Engine merged resources");
		pack.addProperty("pack_format", SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major());
		JsonObject root = new JsonObject();
		root.add("pack", pack);
		Files.writeString(staging.resolve("pack.mcmeta"), root.toString(), StandardCharsets.UTF_8);
	}

	private static void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
		Files.createDirectories(zipFile.getParent());
		try (OutputStream fileOut = Files.newOutputStream(zipFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		     ZipOutputStream zipOut = new ZipOutputStream(fileOut)) {
			try (var stream = Files.walk(sourceDir)) {
				for (Path file : stream.filter(Files::isRegularFile).toList()) {
					String entryName = sourceDir.relativize(file).toString().replace('\\', '/');
					zipOut.putNextEntry(new ZipEntry(entryName));
					try (InputStream in = Files.newInputStream(file)) {
						in.transferTo(zipOut);
					}
					zipOut.closeEntry();
				}
			}
		}
	}
}