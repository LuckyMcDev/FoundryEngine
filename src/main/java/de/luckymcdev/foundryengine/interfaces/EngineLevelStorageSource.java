package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.storage.LevelStorageSource;
import java.nio.file.Path;
import java.util.List;

public interface EngineLevelStorageSource extends EngineInterface<LevelStorageSource> {
    void engine$addAdditionalPath(Path path);

    List<Path> engine$getAdditionalPaths();

    boolean engine$removeAdditionalPath(Path path);

    void engine$clearAdditionalPaths();

    Path engine$resolveWorldPath(String levelId);

    boolean engine$isWorldExternal(String levelId);

    boolean engine$isInstanced(String levelId);

    void engine$deleteInstance(String levelId);

    void engine$clearInstanced();
}
