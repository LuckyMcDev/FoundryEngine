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
		"java.lang.ClassLoader",
		"java.nio.file.Files",
		"java.nio.file.Paths",
		"java.nio.file.FileSystem",
		"java.nio.file.FileSystems"
	);

	private static final List<String> DENIED_PREFIXES = List.of(
		"java.io.",
		"java.net.",
		"java.lang.reflect.",
		"java.lang.invoke.",
		"java.util.prefs.",
		"java.rmi.",
		"javax.script.",
		"sun.",
		"com.sun.",
		"jdk.",
		"jdk.internal.",
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
	 * delegating to the real class loader.
	 */
	public static final class FilteringClassLoader extends ClassLoader {

		private final ClassLoader delegate;

		/**
		 * @param delegate the real class loader to delegate to after the
		 *                 sandbox check passes (must not be null)
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