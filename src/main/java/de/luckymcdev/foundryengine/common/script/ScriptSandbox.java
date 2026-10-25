package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.StartupConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

	private static final Path ACCEPTED_FILE = Common.USER_SPACE.resolve(".sandbox_waiver");
	private static volatile boolean accepted = Files.exists(ACCEPTED_FILE);

	private ScriptSandbox() {
	}

	/**
	 * Returns whether the user has accepted disabling the class filter.
	 * The result is cached; deleting the marker file takes effect after a restart.
	 */
	public static boolean isAccepted() {
		return accepted;
	}

	/**
	 * Creates the acceptance marker file and remembers the acceptance.
	 *
	 * @throws IOException if the file or its parent directory could not be created
	 */
	public static void markAccepted() throws IOException {
		Files.createDirectories(ACCEPTED_FILE.getParent());
		if (Files.notExists(ACCEPTED_FILE)) {
			Files.createFile(ACCEPTED_FILE);
		}
		accepted = true;
	}

	/**
	 * Checks whether a fully qualified class name may be loaded by scripts.
	 * The check is skipped if the class filter is disabled AND accepted.
	 */
	static boolean isClassAllowed(String className) {
		if (isFilterDisabled()) {
			return true;
		}

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
	 * Whether the whole script sandbox (class loader filter and compile-time
	 * AST restrictions) is disabled. Requires the config option AND the
	 * user's acceptance.
	 */
	static boolean isFilterDisabled() {
		return StartupConfig.DISABLE_CLASS_FILTER.get() && accepted;
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