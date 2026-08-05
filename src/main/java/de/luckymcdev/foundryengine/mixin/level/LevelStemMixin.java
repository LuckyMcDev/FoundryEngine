package de.luckymcdev.foundryengine.mixin.level;

import de.luckymcdev.foundryengine.interfaces.level.EngineDimensionOptions;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements {@link EngineDimensionOptions} on LevelStem to control per-dimension save behavior.
 */
@Mixin(LevelStem.class)
public class LevelStemMixin implements EngineDimensionOptions {
	@Unique
	private boolean engine$save = true;
	@Unique
	private boolean engine$saveProperties = true;

	/**
	 * Sets whether this dimension should be saved.
	 */
	@Override
	public void engine$setSave(boolean value) {
		this.engine$save = value;
	}

	/**
	 * Returns whether this dimension should be saved.
	 */
	@Override
	public boolean engine$getSave() {
		return this.engine$save;
	}

	/**
	 * Sets whether this dimension's properties should be saved.
	 */
	@Override
	public void engine$setSaveProperties(boolean value) {
		this.engine$saveProperties = value;
	}

	/**
	 * Returns whether this dimension's properties should be saved.
	 */
	@Override
	public boolean engine$getSaveProperties() {
		return this.engine$saveProperties;
	}
}
