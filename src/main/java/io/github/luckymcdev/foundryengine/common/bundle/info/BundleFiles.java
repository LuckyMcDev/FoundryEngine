package io.github.luckymcdev.foundryengine.common.bundle.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A Record of data related to Bundle Files.
 *
 * @param root      the root path of a bundle
 * @param assets    the assets path of a bundle
 * @param data      the data path of a bundle
 * @param scripts   the scripts paths of a bundle
 */
public record BundleFiles(Path root, Path assets, Path data, List<Path> scripts, @Nullable FileSystem zipFileSystem) {
    private static final Codec<Path> PATH_CODEC = Codec.STRING.xmap(Paths::get, Path::toString);

    public static final Codec<BundleFiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PATH_CODEC.fieldOf("root").forGetter(BundleFiles::root),
            PATH_CODEC.fieldOf("assets").forGetter(BundleFiles::assets),
            PATH_CODEC.fieldOf("data").forGetter(BundleFiles::data),
            PATH_CODEC.listOf().fieldOf("scripts").forGetter(BundleFiles::scripts)
    ).apply(instance, (root, assets, data, scripts) -> new BundleFiles(root, assets, data, scripts, null)));

    public boolean hasZipFileSystem() {
        return zipFileSystem != null && zipFileSystem.isOpen();
    }

    public int scriptCount() {
        return scripts.size();
    }
}