package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.dimension.LevelStem;

import java.util.function.Predicate;

/**
 * Controls dimension save behavior and save properties for level stems.
 */
public interface EngineDimensionOptions extends EngineInterface<LevelStem> {
    Predicate<LevelStem> SAVE_PREDICATE = (e) -> ((EngineDimensionOptions) (Object) e).engine$getSave();
    Predicate<LevelStem> SAVE_PROPERTIES_PREDICATE = (e) -> ((EngineDimensionOptions) (Object) e).engine$getSaveProperties();

    /**
     * Sets whether the dimension data should be saved.
     */
    void engine$setSave(boolean value);

    /**
     * Returns whether the dimension data is saved.
     */
    boolean engine$getSave();

    /**
     * Sets whether the dimension properties should be saved.
     */
    void engine$setSaveProperties(boolean value);

    /**
     * Returns whether the dimension properties are saved.
     */
    boolean engine$getSaveProperties();
}
