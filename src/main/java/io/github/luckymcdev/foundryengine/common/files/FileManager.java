package io.github.luckymcdev.foundryengine.common.files;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager implements ResourceManagerReloadListener {

    public Path createDirectories(Path directory) throws IOException {
        return Files.createDirectories(directory);
    }

    public Path createFile(String path) throws IOException {
        return createFile(Paths.get(path));
    }

    public Path createFile(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                throw new IOException("Path is a directory: " + path);
            }
            return path;
        }
        return Files.createFile(path);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }
}
