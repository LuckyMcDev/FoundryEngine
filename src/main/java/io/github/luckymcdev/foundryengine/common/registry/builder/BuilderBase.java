package io.github.luckymcdev.foundryengine.common.registry.builder;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A Generic Builder, used by {@link BlockBuilder} and {@link ItemBuilder}.
 * Will add a RecipeBuilder / more for any and all registerable things,
 * Will also get a method for making it so I can easily generate scripts
 * for the Editor.
 *
 * @param <T>
 */
public abstract class BuilderBase<T> implements Supplier<T> {
    public final Identifier id;
    public ResourceKey<Registry<T>> registryKey;
    public String translationKey;
    public Component displayName;
    public boolean formattedDisplayName;
    public transient boolean dummyBuilder;
    public transient Set<Identifier> defaultTags;
    protected T object;

    public BuilderBase(Identifier id) {
        this.id = id;
        this.object = null;
        this.translationKey = "";
        this.displayName = null;
        this.formattedDisplayName = false;
        this.dummyBuilder = false;
        this.defaultTags = new HashSet<>();
    }

    public abstract T build();

    public T transformObject(T obj) {
        return obj;
    }

    @Override
    public final T get() {
        try {
            return object;
        } catch (Exception ex) {
            if (dummyBuilder) {
                throw new RuntimeException("Object '" + id + "' of registry '" + registryKey.identifier() + "' is from a dummy builder and doesn't have a value!");
            } else {
                throw new RuntimeException("Object '" + id + "' of registry '" + registryKey.identifier() + "' hasn't been registered yet!", ex);
            }
        }
    }

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

    public BuilderBase<T> translationKey(String key) {
        translationKey = key;
        return this;
    }

    public BuilderBase<T> displayName(Component name) {
        displayName = name;
        return this;
    }

    public BuilderBase<T> formattedDisplayName() {
        formattedDisplayName = true;
        return this;
    }

    public BuilderBase<T> formattedDisplayName(Component name) {
        return formattedDisplayName().displayName(name);
    }

    public BuilderBase<T> tag(Identifier[] tag) {
        defaultTags.addAll(Arrays.asList(tag));
        return this;
    }

    public Identifier newID(String pre, String post) {
        if (pre.isEmpty() && post.isEmpty()) {
            return id;
        }

        return id.withPath(pre + id.getPath() + post);
    }

    public String getBuilderTranslationKey() {
        if (translationKey.isEmpty()) {
            return Util.makeDescriptionId(getTranslationKeyGroup(), id);
        }

        return translationKey;
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