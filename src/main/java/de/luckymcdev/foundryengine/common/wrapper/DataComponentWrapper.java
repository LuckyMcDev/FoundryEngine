package de.luckymcdev.foundryengine.common.wrapper;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

import java.lang.reflect.Field;

public class DataComponentWrapper implements TypeWrapper<DataComponentType<?>> {
    private final String fieldName;

    public DataComponentWrapper(String fieldName) {
        this.fieldName = fieldName;
    }

    public static DataComponentType<?> resolve(String fieldName) {
        return new DataComponentWrapper(fieldName).wrap();
    }

    @Override
    public DataComponentType<?> wrap() {
        try {
            Field field = DataComponents.class.getField(fieldName.toUpperCase());
            return (DataComponentType<?>) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Unknown DataComponents field: '" + fieldName + "'", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access DataComponents field: '" + fieldName + "'", e);
        }
    }
}