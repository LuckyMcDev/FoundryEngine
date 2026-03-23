package de.luckymcdev.foundryengine.common.wrapper;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

import java.lang.reflect.Field;

public class DataComponentWrapper implements TypeWrapper<DataComponentType<?>> {

    public static DataComponentType<?> resolve(String input) {
        return new DataComponentWrapper().wrap(input);
    }

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