package de.luckymcdev.foundryengine.common.script.runtime;

import com.mojang.logging.LogUtils;
import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Result of compiling one bundle: its own class loader, the defined classes in definition
 * order (superclasses first) and everything that failed on the way.
 */
public record CompiledBundle(GroovyClassLoader loader, Map<String, Class<?>> classes, List<ScriptFailure> failures) {

	private static final Logger LOGGER = LogUtils.getLogger();

	public void close() {
		try {
			loader.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close script class loader", e);
		}
	}
}