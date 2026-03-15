package io.github.luckymcdev.foundryengine.server.packs;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.config.Config;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EngineRepositorySource implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackType packType;

    public EngineRepositorySource(PackType packType) {
        this.packType = packType;
    }

    @Override
    public void loadPacks(@NonNull Consumer<Pack> consumer) {
        List<Path> generatedPaths = new ArrayList<>();
        List<Path> manualPaths = new ArrayList<>();


        if (!Config.Startup.RESOURCES_ENABLED.get()) {
            LOGGER.info("Resource loading is disabled in config.");
        }

        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            BundleFiles files = bundle.bundleFiles();

            Path genPath = files.generated().resolve(packType.getDirectory());
            if (Files.exists(genPath)) {
                generatedPaths.add(genPath);
            }

            Path manPath = (packType == PackType.CLIENT_RESOURCES) ? files.assets() : files.data();
            if (Files.exists(manPath)) {
                manualPaths.add(manPath);
            }
        }

        if (!generatedPaths.isEmpty()) {
            loadAggregatePack("bundles_generated", "FoundryEngine: Generated", generatedPaths, consumer, Pack.Position.TOP);
        }
        if (!manualPaths.isEmpty()) {
            loadAggregatePack("bundles_resources", "FoundryEngine: Resources", manualPaths, consumer, Pack.Position.TOP);
        }
    }

    private void loadAggregatePack(String id, String title, List<Path> paths, Consumer<Pack> consumer, Pack.Position position) {
        String packId = "foundry/" + id;
        Component packTitle = Component.literal(title);

        Pack pack = Pack.readMetaAndCreate(
                new PackLocationInfo(packId, packTitle, PackSource.BUILT_IN, Optional.empty()),
                new Pack.ResourcesSupplier() {
                    @Override
                    public @NonNull PackResources openPrimary(PackLocationInfo info) {
                        return new BundlePackResources(info, paths, packType, title);
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                        return new BundlePackResources(info, paths, packType, title);
                    }
                },
                packType,
                new PackSelectionConfig(true, position, false)
        );

        if (pack != null) {
            consumer.accept(pack);
        }
    }
}