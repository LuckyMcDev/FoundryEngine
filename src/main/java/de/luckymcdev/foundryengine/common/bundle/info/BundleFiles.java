package de.luckymcdev.foundryengine.common.bundle.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.luckymcdev.foundryengine.common.bundle.BundleFilesBuilder;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A Record of data related to Bundle Files.
 *
 * @param root          the root path of a bundle
 * @param assets        the assets path of a bundle
 * @param data          the data path of a bundle
 * @param scripts       the ScriptFiles data of a bundle
 * @param zipFileSystem the file system used to open a bundle if its a zip file.
 */
public record BundleFiles(Path root, Path assets, Path data, ScriptFiles scripts, Path saves,
                          @Nullable FileSystem zipFileSystem) {
    private static final Codec<Path> PATH_CODEC = Codec.STRING.xmap(Paths::get, Path::toString);
    public static final Codec<BundleFiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PATH_CODEC.fieldOf("root").forGetter(BundleFiles::root),
            PATH_CODEC.fieldOf("assets").forGetter(BundleFiles::assets),
            PATH_CODEC.fieldOf("data").forGetter(BundleFiles::data),
            ScriptFiles.CODEC.fieldOf("scripts").forGetter(BundleFiles::scripts),
            PATH_CODEC.fieldOf("saves").forGetter(BundleFiles::saves)
    ).apply(instance, (root, assets, data, scripts, saves) -> new BundleFiles(root, assets, data, scripts, saves, null)));

    public static BundleFilesBuilder builder() {
        return new BundleFilesBuilder();
    }

    public boolean hasZipFileSystem() {
        return zipFileSystem != null && zipFileSystem.isOpen();
    }

    public int scriptCount() {
        return scripts.collection().size();
    }

    public record ScriptFiles(Path root, Path client, Path common, Path server, List<Path> collection) {
        public static final Codec<ScriptFiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PATH_CODEC.fieldOf("root").forGetter(ScriptFiles::root),
                PATH_CODEC.fieldOf("client").forGetter(ScriptFiles::client),
                PATH_CODEC.fieldOf("common").forGetter(ScriptFiles::common),
                PATH_CODEC.fieldOf("server").forGetter(ScriptFiles::server),
                PATH_CODEC.listOf().fieldOf("collection").forGetter(ScriptFiles::collection)
        ).apply(instance, ScriptFiles::new));
    }
}