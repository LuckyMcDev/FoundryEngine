package de.luckymcdev.foundryengine.script.common.sandbox;

import java.util.Objects;

/**
 * Parent class loader of every script loader while the sandbox is enabled.
 * When the sandbox is disabled this class is not installed at all.
 */
public final class FilteringClassLoader extends ClassLoader {

	static {
		registerAsParallelCapable();
	}

	public FilteringClassLoader(ClassLoader delegate) {
		super(Objects.requireNonNull(delegate, "delegate"));
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (SandboxRules.isRuntimeDenied(name)) {
			throw new ClassNotFoundException("Class " + name + " is not allowed in scripts");
		}
		return super.loadClass(name, resolve);
	}
}