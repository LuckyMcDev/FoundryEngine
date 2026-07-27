package de.luckymcdev.foundryengine.common.script;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;

/**
 * Sandbox restrictions for Groovy script execution.
 */
@NullMarked
public final class ScriptSandbox {

	private static final List<String> DENIED_CLASSES = List.of(
		"java.lang.Runtime",
		"java.lang.ProcessBuilder",
		"java.lang.Process",
		"java.lang.ProcessHandle",
		"java.lang.Compiler",
		"java.lang.ClassLoader"
	);
	private static final List<String> DENIED_PREFIXES = List.of(
		"java.io.",
		"java.net.",
		"java.nio.",
		"java.lang.reflect.",
		"java.lang.invoke.",
		"jdk.",
		"jdk.internal.",
		"sun.",
		"com.sun.",
		"javax.",
		"org.spongepowered.",
		"org.objectweb.asm."
	);

	private ScriptSandbox() {
	}

	/**
	 * Checks whether a fully qualified class name may be loaded by scripts.
	 */
	static boolean isClassAllowed(String className) {
		Objects.requireNonNull(className);

		if (className.isEmpty() || className.startsWith("[")) {
			return true;
		}

		if (DENIED_CLASSES.contains(className)) {
			return false;
		}
		for (String prefix : DENIED_PREFIXES) {
			if (className.startsWith(prefix)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * A class loader that enforces {@link #isClassAllowed(String)} before
	 * delegating to the real class loader. Intended to be passed as the parent
	 * of a {@link groovy.util.GroovyScriptEngine GroovyScriptEngine} or
	 * {@link groovy.lang.GroovyShell GroovyShell} so that compiled script
	 * code cannot load dangerous classes.
	 */
	public static final class FilteringClassLoader extends ClassLoader {

		private final ClassLoader delegate;

		/**
		 * @param delegate the real class loader to delegate to after the
		 * sandbox check passes (must not be null)
		 */
		public FilteringClassLoader(ClassLoader delegate) {
			super(Objects.requireNonNull(delegate, "delegate"));
			this.delegate = delegate;
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if (!isClassAllowed(name)) {
				throw new ClassNotFoundException("Class " + name + " is not allowed in scripts");
			}
			return super.loadClass(name, resolve);
		}
	}
}