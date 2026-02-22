package io.github.luckymcdev.foundryengine.common.files;

import io.github.luckymcdev.foundryengine.common.Commons;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApiStatus.Internal
public class FileManager implements ResourceManagerReloadListener {

    public FileManager() {
        Commons.requireInternalAccess(this.getClass());
    }

    public void createMainDirectory() throws IOException {
        createDirectories(Commons.FOUNDRY_ENGINE);
        createDirectories(Commons.BUNDLES);
        createDirectories(Commons.CACHE);
        createDirectories(Commons.CONFIG_FE);
    }

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
