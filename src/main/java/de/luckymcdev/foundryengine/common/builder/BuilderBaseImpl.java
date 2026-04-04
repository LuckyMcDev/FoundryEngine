package de.luckymcdev.foundryengine.common.builder;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilderImpl;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilderImpl;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A Generic Builder class for building objects to be registered in the {@link net.neoforged.neoforge.registries.RegisterEvent}.
 * This is the base class for all builders, such as {@link BlockBuilderImpl} and {@link ItemBuilderImpl}.
 * <p>
 * Will also get a method for making it so I can easily generate scripts
 * for the Editor.
 *
 * @param <T> the 'type' of the builder / what is built {@link de.luckymcdev.foundryengine.api.builder.item.ItemBuilder} has {@link net.minecraft.world.item.Item}
 */
public abstract class BuilderBaseImpl<T> implements BuilderBase<T> {
    public final Identifier id;
    public ResourceKey<Registry<T>> registryKey;
    private final boolean dummyBuilder;
    protected T object;

    protected BuilderBaseImpl(Identifier id) {
        this.id = id;
        this.object = null;
        this.dummyBuilder = false;
    }

    @Override
    public abstract T build();

    public T transformObject(T obj) {
        return obj;
    }

    @Override
    public final T get() {
        getOrCreate();
        try {
            return object;
        } catch (Exception ex) {
            if (dummyBuilder) {
                throw new EngineException("Object '" + id + "' of registry '" + registryKey.identifier() + "' is from a dummy builder and doesn't have a value!");
            } else {
                throw new EngineException("Object '" + id + "' of registry '" + registryKey.identifier() + "' hasn't been registered yet!", ex);
            }
        }
    }

    @Override
    public T getOrCreate() {
        if (object == null) {
            createTransformedObject();
        }
        return object;
    }

    public String getTranslationKeyGroup() {
        if (registryKey == null) {
            return "unknown_registry";
        }

        return registryKey.identifier().getPath().replace('/', '.');
    }

    @Override
    public Identifier newID(String pre, String post) {
        if (pre.isEmpty() && post.isEmpty()) {
            return id;
        }

        return id.withPath(pre + id.getPath() + post);
    }

    public T createTransformedObject() {
        object = transformObject(build());
        return object;
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