package de.luckymcdev.foundryengine.mixin.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luckymcdev.foundryengine.interfaces.EngineLevelStorageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelCandidates;
import net.minecraft.world.level.storage.LevelStorageSource.LevelDirectory;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin implements EngineLevelStorageSource {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    private final List<Path> engine$additionalBaseDirs = new ArrayList<>();
    @Shadow
    @Final
    private Path baseDir;

    @Override
    public void engine$addAdditionalPath(Path path) {
        if (path != null && Files.isDirectory(path)) {
            Path normalized = path.toAbsolutePath().normalize();
            if (!engine$additionalBaseDirs.contains(normalized)) {
                engine$additionalBaseDirs.add(normalized);
                LOGGER.info("Added extra world directory via EngineLevelStorageSource: {}", normalized);
            }
        } else {
            LOGGER.warn("Attempted to add invalid or non-existent path: {}", path);
        }
    }

    @Override
    public List<Path> engine$getAdditionalPaths() {
        return Collections.unmodifiableList(engine$additionalBaseDirs);
    }

    @Override
    public boolean engine$removeAdditionalPath(Path path) {
        if (path == null) return false;
        Path normalized = path.toAbsolutePath().normalize();
        boolean removed = engine$additionalBaseDirs.remove(normalized);
        if (removed) {
            LOGGER.info("Removed extra world directory: {}", normalized);
        }
        return removed;
    }

    @Override
    public void engine$clearAdditionalPaths() {
        engine$additionalBaseDirs.clear();
        LOGGER.info("Cleared all extra world directories");
    }

    @Override
    public Path engine$resolveWorldPath(String levelId) {
        return engine$getPath(levelId);
    }

    @Unique
    private Path engine$getPath(String levelId) {
        Path primary = baseDir.resolve(levelId);
        if (Files.isDirectory(primary)) return primary;
        for (Path dir : engine$additionalBaseDirs) {
            Path candidate = dir.resolve(levelId);
            if (Files.isDirectory(candidate)) return candidate;
        }
        return primary;
    }

    @Override
    public boolean engine$isWorldExternal(String levelId) {
        Path primary = baseDir.resolve(levelId);
        if (Files.isDirectory(primary)) return false;
        for (Path dir : engine$additionalBaseDirs) {
            if (Files.isDirectory(dir.resolve(levelId))) return true;
        }
        return false;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void engine$onConstruct(Path baseDir, Path backupDir, DirectoryValidator validator,
                                    com.mojang.datafixers.DataFixer fixerUpper, CallbackInfo ci) {
        engine$additionalBaseDirs.addAll(EngineLevelStorageSource.GLOBAL_ADDITIONAL_PATHS);
    }

    @ModifyReturnValue(method = "getLevelPath", at = @At("RETURN"))
    private Path engine$modifyGetLevelPath(Path original, String levelId) {
        return engine$getPath(levelId);
    }

    @ModifyReturnValue(method = "findLevelCandidates", at = @At("RETURN"))
    private LevelCandidates engine$modifyFindLevelCandidates(LevelCandidates original) throws LevelStorageException {
        List<LevelDirectory> all = new ArrayList<>(original.levels());
        for (Path dir : engine$additionalBaseDirs) {
            engine$addCandidatesFromDir(dir, all);
        }
        return new LevelCandidates(all);
    }

    @Unique
    private void engine$addCandidatesFromDir(Path dir, List<LevelDirectory> out) throws LevelStorageException {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> streams = Files.list(dir)) {
            streams.filter(Files::isDirectory)
                    .map(LevelDirectory::new)
                    .filter(d -> Files.isRegularFile(d.dataFile()) || Files.isRegularFile(d.oldDataFile()))
                    .forEach(out::add);
        } catch (IOException e) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
        }
    }
}