package io.github.luckymcdev.foundryengine.server.packs;

import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class BundlePackResources extends AbstractPackResources {
    private final Path root;
    private final PackType packType;
    private final String bundleId;

    public BundlePackResources(PackLocationInfo info, Path root, PackType packType, String bundleId) {
        super(info);
        this.root = root;
        this.packType = packType;
        this.bundleId = bundleId;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && paths[0].equals("pack.mcmeta")) {
            return () -> new ByteArrayInputStream(generatePackMeta().getBytes());
        }
        Path file = root.resolve(String.join("/", paths));
        return Files.exists(file) ? () -> Files.newInputStream(file) : null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type != packType) return null;

        // Don't add type.getDirectory() again since root is already assets/ or data/
        Path file = root
                .resolve(location.getNamespace())
                .resolve(location.getPath());

        return Files.exists(file) ? () -> Files.newInputStream(file) : null;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type != packType) return;

        // Don't add type.getDirectory() since root is already assets/ or data/
        Path namespacePath = root.resolve(namespace);

        if (!Files.isDirectory(namespacePath)) return;
        try (var stream = Files.walk(namespacePath)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                String relative = namespacePath.relativize(file).toString().replace('\\', '/');
                if (relative.startsWith(prefix)) {
                    output.accept(Identifier.fromNamespaceAndPath(namespace, relative),
                            () -> Files.newInputStream(file));
                }
            });
        } catch (IOException ignored) {
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != packType) return Set.of();

        // root is already assets/ or data/, so list directly
        if (!Files.isDirectory(root)) return Set.of();

        try (var stream = Files.list(root)) {
            return stream.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
        }
    }

    private String packTypeName() {
        return switch (packType) {
            case CLIENT_RESOURCES -> "Assets";
            case SERVER_DATA -> "Data";
        };
    }

    private String generatePackMeta() {
        PackFormat format = SharedConstants.getCurrentVersion().packVersion(packType);
        int major = format.major();
        return """
                {
                  "pack": {
                    "description": "Bundle %s (%s)",
                    "pack_format": %d,
                    "min_format": %d,
                    "max_format": %d
                  }
                }
                """.formatted(packTypeName(), bundleId, major, major, major);
    }

    @Override
    public void close() {
    }
}