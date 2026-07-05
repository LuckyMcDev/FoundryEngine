package de.luckymcdev.foundryengine.common.util;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.mixin.resource.FilePackResourcesAccessor;
import de.luckymcdev.foundryengine.mixin.resource.PathPackResourcesAccessor;
import de.luckymcdev.foundryengine.mixin.resource.SharedZipFileAccessAccessor;
import de.luckymcdev.foundryengine.mixin.resource.VanillaPackResourcesAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.resource.EmptyPackResources;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Lists every resource in every loaded pack by walking the real backing filesystem paths
 * directly, instead of going through ResourceManager#listResources(path, predicate).
 */
public final class PackResourceScanner {
	private static final Logger LOGGER = LogUtils.getLogger();

	private PackResourceScanner() {
	}

	public static void scanAll(ResourceManager manager, PackType type, BiConsumer<Identifier, Path> consumer) {
		manager.listPacks().forEach(pack -> scanPack(pack, type, consumer));
	}

	private static void scanPack(PackResources pack, PackType type, BiConsumer<Identifier, Path> consumer) {
		try {
			if (pack instanceof JarContentsPackResources jarPack) {
				return; // JarContentsPackResources needs to be skipped.
			}

			if (pack instanceof EmptyPackResources) {
				return; // genuinely nothing to scan
			}

			if (pack instanceof PathPackResources pathPack) {
				Path root = ((PathPackResourcesAccessor) pathPack).engine$getRoot();
				if (root != null) {
					walkDirectory(root.resolve(type.getDirectory()), consumer);
				}
				return;
			}

			if (pack instanceof VanillaPackResources vanillaPack) {
				Map<PackType, List<Path>> pathsForType = ((VanillaPackResourcesAccessor) vanillaPack).engine$getPathsForType();
				if (pathsForType != null) {
					List<Path> roots = pathsForType.get(type);
					if (roots != null) {
						for (Path root : roots) {
							walkDirectory(root, consumer);
						}
					}
				}
				return;
			}

			if (pack instanceof FilePackResources filePack) {
				Path zipFile = ((SharedZipFileAccessAccessor) ((FilePackResourcesAccessor) filePack).engine$zipFileAccess()).engine$getFile().toPath();
				if (zipFile != null && Files.isRegularFile(zipFile)) {
					String prefix = ((FilePackResourcesAccessor) filePack).engine$prefix();
					walkZipFile(zipFile, prefix, type, consumer);
				}
				return;
			}

			List<Path> roots = findPathsGenerically(pack, new IdentityHashMap<>(), 0);
			if (roots.isEmpty()) {
				LOGGER.debug("Couldn't find a Path on pack resource type {} - " +
						"if this is your own PackResources, add a getRoot(PackType) method to it instead",
					pack.getClass().getName());
				return;
			}
			for (Path root : roots) {
				Path withTypeDir = root.resolve(type.getDirectory());
				walkDirectory(Files.isDirectory(withTypeDir) ? withTypeDir : root, consumer);
			}
		} catch (Exception e) {
			LOGGER.debug("Failed to walk pack resources for {}: {}", pack.packId(), e.getMessage());
		}
	}

	private static List<Path> findPathsGenerically(Object obj, Map<Object, Boolean> visited, int depth) {
		List<Path> found = new ArrayList<>();
		if (obj == null || depth > 2 || visited.containsKey(obj)) {
			return found;
		}
		visited.put(obj, Boolean.TRUE);

		Class<?> cls = obj.getClass();
		while (cls != null && cls != Object.class) {
			for (Field field : cls.getDeclaredFields()) {
				try {
					field.setAccessible(true);
					Object value = field.get(obj);
					if (value instanceof Path p) {
						found.add(p);
					} else if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Path) {
						for (Object o : list) {
							found.add((Path) o);
						}
					} else if (value != null) {
						String pkg = value.getClass().getPackageName();
						if (!pkg.startsWith("java.") && !pkg.startsWith("javax.")) {
							found.addAll(findPathsGenerically(value, visited, depth + 1));
						}
					}
				} catch (Exception ignored) {
				}
			}
			cls = cls.getSuperclass();
		}
		return found;
	}

	private static void walkDirectory(Path dir, BiConsumer<Identifier, Path> consumer) {
		if (!Files.isDirectory(dir)) {
			return;
		}
		String separator = dir.getFileSystem().getSeparator();
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					String relative = dir.relativize(file).toString().replace(separator, "/");
					String[] parts = relative.split("/", 2);
					if (parts.length == 2) {
						Identifier id = Identifier.tryParse(parts[0] + ":" + parts[1]);
						if (id != null) {
							consumer.accept(id, file);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOGGER.debug("Failed to walk directory {}: {}", dir, e.getMessage());
		}
	}

	private static void walkZipFile(Path zipPath, String prefix, PackType type, BiConsumer<Identifier, Path> consumer) {
		String internalPath = prefix.isEmpty() ? type.getDirectory() : prefix + "/" + type.getDirectory();
		try (FileSystem fs = FileSystems.newFileSystem(zipPath, (ClassLoader) null)) {
			Path root = fs.getPath(internalPath);
			if (Files.isDirectory(root)) {
				walkDirectory(root, consumer);
			} else {
				LOGGER.debug("Path '{}' not found in zip {}", internalPath, zipPath);
			}
		} catch (IOException e) {
			LOGGER.debug("Failed to open zip {}: {}", zipPath, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getField(Object instance, Class<?> owner, String name) throws Exception {
		Field field = owner.getDeclaredField(name);
		field.setAccessible(true);
		return (T) field.get(instance);
	}
}