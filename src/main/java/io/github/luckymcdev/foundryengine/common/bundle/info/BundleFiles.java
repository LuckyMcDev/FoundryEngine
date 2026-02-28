package io.github.luckymcdev.foundryengine.common.bundle.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public record BundleFiles(Path root, Path assets, Path data, List<Path> scripts) {
    private static final Codec<Path> PATH_CODEC = Codec.STRING.xmap(Paths::get, Path::toString);

    public static final Codec<BundleFiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PATH_CODEC.fieldOf("root").forGetter(BundleFiles::root),
            PATH_CODEC.fieldOf("assets").forGetter(BundleFiles::assets),
            PATH_CODEC.fieldOf("data").forGetter(BundleFiles::data),
            PATH_CODEC.listOf().fieldOf("scripts").forGetter(BundleFiles::scripts)
    ).apply(instance, BundleFiles::new));

    public int getScriptCount() {
        return scripts.size();
    }
}