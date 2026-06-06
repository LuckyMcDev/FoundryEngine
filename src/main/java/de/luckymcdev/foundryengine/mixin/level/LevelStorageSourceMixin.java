package de.luckymcdev.foundryengine.mixin.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.luckymcdev.foundryengine.common.world.StorageSourceManager;
import de.luckymcdev.foundryengine.config.CommonConfig;
import de.luckymcdev.foundryengine.interfaces.EngineLevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelCandidates;
import net.minecraft.world.level.storage.LevelStorageSource.LevelDirectory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin implements EngineLevelStorageSource {
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Path baseDir;

    @Override
    public void engine$addAdditionalPath(Path path) {
        StorageSourceManager.addAdditionalPath(path);
    }

    @Override
    public List<Path> engine$getAdditionalPaths() {
        return StorageSourceManager.getAdditionalPaths();
    }

    @Override
    public boolean engine$removeAdditionalPath(Path path) {
        return StorageSourceManager.removeAdditionalPath(path);
    }

    @Override
    public void engine$clearAdditionalPaths() {
        StorageSourceManager.clearAdditionalPaths();
    }

    @Override
    public Path engine$resolveWorldPath(String levelId) {
        return StorageSourceManager.resolveWorldPath(baseDir, levelId);
    }

    @Override
    public boolean engine$isWorldExternal(String levelId) {
        return StorageSourceManager.isWorldExternal(baseDir, levelId);
    }

    @Override
    public boolean engine$isInstanced(String levelId) {
        return StorageSourceManager.isInstanced(levelId);
    }

    @Override
    public void engine$deleteInstance(String levelId) {
        StorageSourceManager.deleteInstance(levelId);
    }

    @Override
    public void engine$clearInstanced() {
        StorageSourceManager.clearInstanced();
    }

    @ModifyReturnValue(method = "getLevelPath", at = @At("RETURN"))
    private Path engine$modifyGetLevelPath(Path original, String levelId) {
        if (!CommonConfig.PACK_MODE.get().equalsIgnoreCase("dev")) {
            Path instanced = StorageSourceManager.getInstancedPath(levelId);
            if (instanced != null) return instanced;
        }
        return StorageSourceManager.resolveWorldPath(baseDir, levelId);
    }

    @ModifyReturnValue(method = "findLevelCandidates", at = @At("RETURN"))
    private LevelCandidates engine$modifyFindLevelCandidates(LevelCandidates original) {
        List<LevelDirectory> all = new ArrayList<>(original.levels());
        StorageSourceManager.addCandidatesFromAdditionalDirs(all);
        return new LevelCandidates(all);
    }

    @Inject(method = "createAccess", at = @At("HEAD"))
    private void engine$onCreateAccess(String levelId, CallbackInfoReturnable<LevelStorageSource.LevelStorageAccess> cir) throws IOException {
        if (StorageSourceManager.isWorldExternal(baseDir, levelId)) {
            StorageSourceManager.ensureInstanced(levelId, StorageSourceManager.resolveWorldPath(baseDir, levelId));
        }
    }

    @Inject(method = "validateAndCreateAccess", at = @At("HEAD"))
    private void engine$onValidateAndCreateAccess(String levelId, CallbackInfoReturnable<LevelStorageSource.LevelStorageAccess> cir) throws IOException {
        if (StorageSourceManager.isWorldExternal(baseDir, levelId)) {
            StorageSourceManager.ensureInstanced(levelId, StorageSourceManager.resolveWorldPath(baseDir, levelId));
        }
    }
}
