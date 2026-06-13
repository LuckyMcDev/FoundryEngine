package de.luckymcdev.foundryengine.common.builder;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBuilder<T> implements BuilderBase<T> {
    protected final Identifier id;
    protected boolean generateData = true;
    protected @Nullable T object;

    protected AbstractBuilder(Identifier id) {
        this.id = id;
    }

    @Override
    public T get() {
        if (object == null) {
            throw new IllegalStateException(id + " has not been registered yet");
        }
        return object;
    }

    @Override
    public T getOrCreate() {
        if (object == null) {
            object = build();
        }
        return object;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Identifier newID(String pre, String post) {
        if (pre.isEmpty() && post.isEmpty()) {
            return id;
        }
        return id.withPath(pre + id.getPath() + post);
    }

    @Override
    public boolean shouldGenerateData() {
        return generateData;
    }

    protected void setObject(T object) {
        this.object = object;
    }
}
