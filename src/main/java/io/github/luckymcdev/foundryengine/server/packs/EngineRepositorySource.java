package io.github.luckymcdev.foundryengine.server.packs;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.info.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public class EngineRepositorySource implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackType packType;

    public EngineRepositorySource(PackType packType) {
        this.packType = packType;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        Common.getBundleManager().getBundles().forEach(bundle -> {
            LOGGER.debug("Registering pack for bundle: {}", bundle.info().getId());
            loadPackFor(bundle, consumer);
        });
    }

    private void loadPackFor(Bundle bundle, Consumer<Pack> consumer) {
        String id = bundle.info().getId();
        BundleFiles files = bundle.bundleFiles();

        Path path = packType == PackType.CLIENT_RESOURCES ? files.assets() : files.data();
        String label = packType == PackType.CLIENT_RESOURCES ? "Assets" : "Data";

        Pack pack = Pack.readMetaAndCreate(
                new PackLocationInfo("bundle/" + id + "/" + label.toLowerCase(),
                        Component.literal(id + " " + label),
                        PackSource.DEFAULT, Optional.empty()),
                supplier(path, packType, id),
                packType,
                new PackSelectionConfig(true, Pack.Position.TOP, false)
        );

        if (pack != null) consumer.accept(pack);
    }

    private Pack.ResourcesSupplier supplier(Path path, PackType packType, String bundleId) {
        return new Pack.ResourcesSupplier() {
            @Override
            public @NonNull PackResources openPrimary(@NonNull PackLocationInfo info) {
                return new BundlePackResources(info, path, packType, bundleId);
            }

            @Override
            public @NonNull PackResources openFull(@NonNull PackLocationInfo info, Pack.@NonNull Metadata metadata) {
                return new BundlePackResources(info, path, packType, bundleId);
            }
        };
    }
}