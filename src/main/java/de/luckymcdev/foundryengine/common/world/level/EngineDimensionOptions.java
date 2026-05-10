package de.luckymcdev.foundryengine.common.world.level;

import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.Internal
public interface EngineDimensionOptions {
    Predicate<LevelStem> SAVE_PREDICATE = (e) -> ((EngineDimensionOptions) (Object) e).engine$getSave();
    Predicate<LevelStem> SAVE_PROPERTIES_PREDICATE = (e) -> ((EngineDimensionOptions) (Object) e).engine$getSaveProperties();

    void engine$setSave(boolean value);

    boolean engine$getSave();

    void engine$setSaveProperties(boolean value);

    boolean engine$getSaveProperties();
}
