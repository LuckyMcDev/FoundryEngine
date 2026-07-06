package de.luckymcdev.foundryengine.common.builder;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBuilder<T> {
	protected final Identifier id;
	protected boolean generateData = true;
	protected @Nullable T object;

	protected AbstractBuilder(Identifier id) {
		this.id = id;
	}

	public T get() {
		if (object == null) {
			throw new IllegalStateException(id + " has not been registered yet");
		}
		return object;
	}

	protected abstract T build();

	public T getOrCreate() {
		if (object == null) {
			object = build();
		}
		return object;
	}

	public Identifier getId() {
		return id;
	}

	public Identifier newID(String pre, String post) {
		if (pre.isEmpty() && post.isEmpty()) {
			return id;
		}
		return id.withPath(pre + id.getPath() + post);
	}

	public boolean shouldGenerateData() {
		return generateData;
	}

	protected void setObject(T object) {
		this.object = object;
	}
}
