package de.luckymcdev.foundryengine.common.wrapper;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

import java.lang.reflect.Field;

/**
 * Resolves {@link DataComponentType} by field name from {@link DataComponents}.
 */
public class DataComponentWrapper implements TypeWrapper<DataComponentType<?>> {

	/**
	 * Resolves a DataComponentType by its field name in {@link DataComponents}.
	 */
	public static DataComponentType<?> resolve(String input) {
		return new DataComponentWrapper().wrap(input);
	}

	/**
	 * Wraps the input string by resolving it as a DataComponents field name.
	 */
	@Override
	public DataComponentType<?> wrap(String input) {
		try {
			Field field = DataComponents.class.getField(input.toUpperCase());
			return (DataComponentType<?>) field.get(null);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Unknown DataComponents field: '" + input + "'", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Could not access DataComponents field: '" + input + "'", e);
		}
	}
}