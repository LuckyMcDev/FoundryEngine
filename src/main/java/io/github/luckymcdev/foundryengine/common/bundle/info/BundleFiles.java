package io.github.luckymcdev.foundryengine.common.bundle.info;

import java.nio.file.Path;
import java.util.List;

public record BundleFiles(Path root, Path assets, Path data, List<Path> scripts) {

    public int getScriptCount() {
        return scripts.size();
    }
}