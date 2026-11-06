package de.luckymcdev.foundryengine.script.common.runtime;

import com.mojang.logging.LogUtils;
import groovy.lang.GroovyClassLoader;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Result of compiling one bundle: its own Groovy class loader, the (optional) library class
 * loader, the defined classes in definition order (superclasses first) and everything that
 * failed on the way.
 *
 * <p>Both loaders are closed by {@link #close()}, in child-to-parent order.
 */
public record CompiledBundle(
	GroovyClassLoader loader,
	@Nullable ClassLoader libraryLoader,
	Map<String, Class<?>> classes,
	List<ScriptFailure> failures
) {

	private static final Logger LOGGER = LogUtils.getLogger();

	public void close() {
		try {
			loader.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close script class loader", e);
		}
		if (libraryLoader instanceof Closeable closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOGGER.warn("Failed to close library class loader", e);
			}
		}
	}
}