package de.luckymcdev.foundryengine.interfaces.level;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
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
	default void engine$setSave(boolean value) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether the dimension data is saved.
	 */
	default boolean engine$getSave() {
		throw new NoMixinException(this);
	}

	/**
	 * Sets whether the dimension properties should be saved.
	 */
	default void engine$setSaveProperties(boolean value) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether the dimension properties are saved.
	 */
	default boolean engine$getSaveProperties() {
		throw new NoMixinException(this);
	}
}
