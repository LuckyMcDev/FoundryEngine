package de.luckymcdev.foundryengine.interfaces;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface EngineLevelStorageSource {
    List<Path> GLOBAL_ADDITIONAL_PATHS = new ArrayList<>();

    void engine$addAdditionalPath(Path path);

    List<Path> engine$getAdditionalPaths();

    boolean engine$removeAdditionalPath(Path path);

    void engine$clearAdditionalPaths();

    Path engine$resolveWorldPath(String levelId);

    boolean engine$isWorldExternal(String levelId);
}
