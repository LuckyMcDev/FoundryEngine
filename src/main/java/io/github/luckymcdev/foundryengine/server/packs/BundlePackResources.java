package io.github.luckymcdev.foundryengine.server.packs;

import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BundlePackResources extends AbstractPackResources {
    private final List<Path> roots;
    private final PackType packType;
    private final String packName;

    public BundlePackResources(PackLocationInfo info, List<Path> roots, PackType packType, String packName) {
        super(info);
        this.roots = roots;
        this.packType = packType;
        this.packName = packName;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && paths[0].equals("pack.mcmeta")) {
            return () -> new ByteArrayInputStream(generatePackMeta().getBytes());
        }

        String relativePath = String.join("/", paths);

        for (Path rootPath : roots) {
            Path file = rootPath.resolve(relativePath);
            if (Files.exists(file)) {
                return () -> Files.newInputStream(file);
            }
        }
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type != packType) return null;

        for (Path root : roots) {
            Path path = root.resolve(location.getNamespace()).resolve(location.getPath());
            if (Files.exists(path)) {
                return IoSupplier.create(path);
            }
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type != packType) return;

        for (Path root : roots) {
            Path namespacePath = root.resolve(namespace);
            if (!Files.isDirectory(namespacePath)) continue;

            try (var files = Files.walk(namespacePath)) {
                files.filter(Files::isRegularFile).forEach(file -> {
                    String relative = namespacePath.relativize(file).toString().replace('\\', '/');
                    if (relative.startsWith(prefix)) {
                        output.accept(Identifier.fromNamespaceAndPath(namespace, relative),
                                () -> Files.newInputStream(file));
                    }
                });
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public @NonNull Set<String> getNamespaces(@NonNull PackType type) {
        if (type != packType) return Set.of();

        Set<String> namespaces = new HashSet<>();
        for (Path root : roots) {
            if (!Files.isDirectory(root)) continue;
            try (var stream = Files.list(root)) {
                stream.filter(Files::isDirectory)
                        .map(p -> p.getFileName().toString())
                        .forEach(namespaces::add);
            } catch (IOException ignored) {
            }
        }
        return namespaces;
    }

    private String generatePackMeta() {
        PackFormat format = SharedConstants.getCurrentVersion().packVersion(packType);
        int major = format.major();
        return """
                {
                  "pack": {
                    "description": "%s",
                    "pack_format": %d,
                    "min_format": %d,
                    "max_format": %d
                  }
                }
                """.formatted(packName, major, major, major);
    }

    @Override
    public void close() {
        roots.clear();
    }
}