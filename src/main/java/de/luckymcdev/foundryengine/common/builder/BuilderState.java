package de.luckymcdev.foundryengine.common.builder;

import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates common state management for all builders.
 * This allows builders to use composition instead of inheritance.
 *
 * @param <T> The type of object being built
 */
public class BuilderState<T> {
    public final Identifier id;
    public @Nullable ResourceKey<Registry<T>> registryKey;
    public @Nullable T object;

    public BuilderState(Identifier id) {
        this.id = id;
        this.object = null;
    }

    public @Nullable T get() {
        getOrCreate();
        try {
            return object;
        } catch (Exception ex) {
            throw new EngineException(
                    "Object '" + id + "' of registry '" + registryKey.identifier() + "' hasn't been registered yet!",
                    ex
            );
        }
    }

    public T getOrCreate() {
        if (object == null) {
            object = build();
        }
        return object;
    }

    public Identifier newID(String pre, String post) {
        if (pre.isEmpty() && post.isEmpty()) {
            return id;
        }
        return id.withPath(pre + id.getPath() + post);
    }

    /**
     * Called to build the actual object. Must be implemented by subclasses or set via functional interface.
     */
    protected T build() {
        throw new UnsupportedOperationException("build() must be implemented");
    }

    public void setObject(@Nullable T obj) {
        this.object = obj;
    }

    public boolean isRegistered() {
        return object != null;
    }

    @Override
    public String toString() {
        var n = getClass().getName();
        int i = n.lastIndexOf('.');
        if (i != -1) {
            n = n.substring(i + 1);
        }
        return n + "[" + id + "]";
    }
}