package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;

import java.io.IOException;

/**
 * Interface for script engines that can compile and load bundle scripts.
 */
public interface BundleScriptEngine {

	String fileExtension();

	void initialize(BundleFiles files) throws IOException;

	Class<?> loadClass(String scriptName) throws Exception;

	void close();
}