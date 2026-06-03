package de.luckymcdev.foundryengine.server.packs;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

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
    private final boolean fixedPosition;

    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier
    ) {
        this(packType, packId, packTitle, pathsSupplier, Pack.Position.BOTTOM);
    }

    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier,
            Pack.Position position
    ) {
        this(packType, packId, packTitle, pathsSupplier, position, false);
    }

    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier,
            Pack.Position position,
            boolean fixedPosition
    ) {
        this.packType = packType;
        this.packId = packId;
        this.packTitle = packTitle;
        this.pathsSupplier = pathsSupplier;
        this.position = position;
        this.fixedPosition = fixedPosition;
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

        var info = new PackLocationInfo(packId, Component.literal(packTitle), PackSource.BUILT_IN, Optional.empty());

        List<PackResources> children = paths.stream()
                .map(path -> new PathPackResources(info, path, packType))
                .map(PackResources.class::cast)
                .toList();

        Pack pack = getPack(info, children);

        consumer.accept(pack);
    }

    private @NonNull Pack getPack(PackLocationInfo info, List<PackResources> children) {
        PackResources composite = new CompositePackResources(info, children, packType);

        Pack pack = new Pack(
                info,
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(PackLocationInfo info) {
                        return composite;
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                        return composite;
                    }
                },
                new Pack.Metadata(
                        Component.literal(packTitle),
                        PackCompatibility.COMPATIBLE,
                        FeatureFlagSet.of(),
                        List.of()
                ),
                new PackSelectionConfig(true, position, fixedPosition)
        );
        return pack;
    }

    private static final class PathPackResources extends AbstractPackResources {
        private final Path root;
        private final PackType packType;

        private PathPackResources(PackLocationInfo info, Path root, PackType packType) {
            super(info);
            this.root = root;
            this.packType = packType;
        }

        @Override
        public IoSupplier<InputStream> getRootResource(String... paths) {
            String relative = String.join("/", paths);
            Path file = root.resolve(relative);
            if (Files.exists(file)) {
                return () -> Files.newInputStream(file);
            }
            return null;
        }

        @Override
        public IoSupplier<InputStream> getResource(@NonNull PackType type, @NonNull Identifier location) {
            if (type != packType) return null;

            Path file = root.resolve(location.getNamespace()).resolve(location.getPath());
            if (Files.exists(file)) {
                return IoSupplier.create(file);
            }
            return null;
        }

        @Override
        public void listResources(@NonNull PackType type, @NonNull String namespace, @NonNull String prefix, @NonNull ResourceOutput output) {
            if (type != packType) return;

            Path namespacePath = root.resolve(namespace);
            if (!Files.isDirectory(namespacePath)) return;

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
            }
        }

        @Override
        public @NonNull Set<String> getNamespaces(@NonNull PackType type) {
            if (type != packType) return Set.of();

            Set<String> namespaces = new HashSet<>();
            if (!Files.isDirectory(root)) return namespaces;
            try (var stream = Files.list(root)) {
                stream.filter(Files::isDirectory)
                        .map(p -> p.getFileName().toString())
                        .forEach(namespaces::add);
            } catch (IOException ignored) {
            }
            return namespaces;
        }

        @Override
        public void close() {
        }
    }
}
