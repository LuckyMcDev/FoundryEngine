package de.luckymcdev.foundryengine.server.packs;

import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicPackRepository implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PackType packType;
    private final String packId;
    private final String packTitle;
    private final Supplier<List<Path>> pathsSupplier;
    private final Pack.Position position;

    /**
     * @param packType      CLIENT_RESOURCES or SERVER_DATA
     * @param packId        Unique pack identifier
     * @param packTitle     Human-readable name shown in pack screens
     * @param pathsSupplier Called at load time — return all root paths to include.
     *                      Empty or non-existent paths are silently skipped.
     */
    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier
    ) {
        this(packType, packId, packTitle, pathsSupplier, Pack.Position.TOP);
    }

    /**
     * Full constructor with explicit pack position.
     */
    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier,
            Pack.Position position
    ) {
        this.packType = packType;
        this.packId = packId;
        this.packTitle = packTitle;
        this.pathsSupplier = pathsSupplier;
        this.position = position;
    }

    @Override
    public void loadPacks(@NonNull Consumer<Pack> consumer) {
        List<Path> paths = pathsSupplier.get().stream()
                .filter(Files::exists)
                .toList();

        if (paths.isEmpty()) {
            LOGGER.debug("[FoundryEngine] No paths available for pack '{}', skipping.", packId);
            return;
        }

        Pack pack = Pack.readMetaAndCreate(
                new PackLocationInfo(packId, Component.literal(packTitle), PackSource.BUILT_IN, Optional.empty()),
                new Pack.ResourcesSupplier() {
                    @Override
                    public @NonNull PackResources openPrimary(PackLocationInfo info) {
                        return new Resources(info, paths, packType, packTitle);
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                        return new Resources(info, paths, packType, packTitle);
                    }
                },
                packType,
                new PackSelectionConfig(true, position, false)
        );

        if (pack != null) {
            consumer.accept(pack);
        }
    }

    private static final class Resources extends AbstractPackResources {

        private final List<Path> roots;
        private final PackType packType;
        private final String packName;

        private Resources(PackLocationInfo info, List<Path> roots, PackType packType, String packName) {
            super(info);
            this.roots = roots;
            this.packType = packType;
            this.packName = packName;
        }

        @Override
        public IoSupplier<@NonNull InputStream> getRootResource(String... paths) {
            if (paths.length == 1 && paths[0].equals("pack.mcmeta")) {
                return () -> new ByteArrayInputStream(buildPackMeta().getBytes());
            }

            String relative = String.join("/", paths);
            for (Path root : roots) {
                Path file = root.resolve(relative);
                if (Files.exists(file)) {
                    return () -> Files.newInputStream(file);
                }
            }
            return null;
        }

        @Override
        public IoSupplier<@NonNull InputStream> getResource(@NonNull PackType type, @NonNull Identifier location) {
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
        public void listResources(@NonNull PackType type, @NonNull String namespace, @NonNull String prefix, @NonNull ResourceOutput output) {
            if (type != packType) return;

            for (Path root : roots) {
                Path namespacePath = root.resolve(namespace);
                if (!Files.isDirectory(namespacePath)) continue;

                try (var files = Files.walk(namespacePath)) {
                    files.filter(Files::isRegularFile).forEach(file -> {
                        String relative = namespacePath.relativize(file).toString().replace('\\', '/');
                        if (relative.startsWith(prefix)) {
                            output.accept(
                                    Identifier.fromNamespaceAndPath(namespace, relative),
                                    () -> Files.newInputStream(file)
                            );
                        }
                    });
                } catch (IOException ignored) {
                    // ignored
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
                    // ignored
                }
            }
            return namespaces;
        }

        private String buildPackMeta() {
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
        }
    }
}