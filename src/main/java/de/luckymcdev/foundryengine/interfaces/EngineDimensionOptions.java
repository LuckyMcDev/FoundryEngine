package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.dimension.LevelStem;

import java.util.function.Predicate;

public interface EngineDimensionOptions extends EngineInterface<LevelStem> {
    Predicate<LevelStem> SAVE_PREDICATE = (e) -> ((EngineDimensionOptions) (Object) e).engine$getSave();
    Predicate<LevelStem> SAVE_PROPERTIES_PREDICATE = (e) -> ((EngineDimensionOptions) (Object) e).engine$getSaveProperties();

    void engine$setSave(boolean value);

    boolean engine$getSave();

    void engine$setSaveProperties(boolean value);

    boolean engine$getSaveProperties();
}
