package de.luckymcdev.foundryengine.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link RepositorySource} that serves paths from a dynamic supplier as a virtual resource pack.
 */
public class DynamicPackRepository implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PackType packType;
    private final String packId;
    private final String packTitle;
    private final String description;
    private final Supplier<List<Path>> pathsSupplier;
    private final Pack.Position position;
    private final boolean fixedPosition;

    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier
    ) {
        this(packType, packId, packTitle, packTitle, pathsSupplier, Pack.Position.BOTTOM, false);
    }

    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier,
            Pack.Position position
    ) {
        this(packType, packId, packTitle, packTitle, pathsSupplier, position, false);
    }

    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            Supplier<List<Path>> pathsSupplier,
            Pack.Position position,
            boolean fixedPosition
    ) {
        this(packType, packId, packTitle, packTitle, pathsSupplier, position, fixedPosition);
    }

    /**
     * Full constructor. Creates a virtual pack containing files from all {@code pathsSupplier} directories.
     */
    public DynamicPackRepository(
            PackType packType,
            String packId,
            String packTitle,
            String description,
            Supplier<List<Path>> pathsSupplier,
            Pack.Position position,
            boolean fixedPosition
    ) {
        this.packType = packType;
        this.packId = packId;
        this.packTitle = packTitle;
        this.description = description;
        this.pathsSupplier = pathsSupplier;
        this.position = position;
        this.fixedPosition = fixedPosition;
    }

    private static byte[] buildPackMeta(PackType packType, String description) {
        JsonObject pack = new JsonObject();
        pack.addProperty("description", description);
        pack.addProperty("pack_format", SharedConstants.getCurrentVersion().packVersion(packType).major());
        JsonObject root = new JsonObject();
        root.add("pack", pack);
        return root.toString().getBytes(StandardCharsets.UTF_8);
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
        byte[] packMeta = buildPackMeta(packType, description);

        Pack pack = new Pack(
                info,
                new Pack.ResourcesSupplier() {
                    @Override
                    public @NonNull PackResources openPrimary(PackLocationInfo info) {
                        return new Resources(info, paths, packType, packMeta);
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                        return new Resources(info, paths, packType, packMeta);
                    }
                },
                new Pack.Metadata(
                        Component.literal(description),
                        PackCompatibility.COMPATIBLE,
                        FeatureFlagSet.of(),
                        List.of()
                ),
                new PackSelectionConfig(true, position, fixedPosition)
        );

        consumer.accept(pack);
    }

    private static final class Resources extends AbstractPackResources {
        private final List<Path> roots;
        private final PackType packType;
        private final byte[] packMeta;

        private Resources(PackLocationInfo info, List<Path> roots, PackType packType, byte[] packMeta) {
            super(info);
            this.roots = roots;
            this.packType = packType;
            this.packMeta = packMeta;
        }

		public List<Path> getRoot(PackType type) {
			if (type == this.packType) {
				return List.copyOf(roots);
			}
			return List.of();
		}

        @Override
        public IoSupplier<@NonNull InputStream> getRootResource(String... paths) {
            if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
                return () -> new ByteArrayInputStream(packMeta);
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

        @Override
        public void close() {
        }
    }
}
