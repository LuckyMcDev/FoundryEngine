package de.luckymcdev.foundryengine.script.common.runtime;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * Class loader for a bundle's downloaded Maven libraries. Parent is the host (Minecraft)
 * loader, so library code is not subject to the script sandbox - it is trusted code that
 * scripts may access through the {@code FilteringClassLoader} in the chain above it.
 * <p>
 * One instance per bundle. The engine closes it during bundle unload.
 */
public final class BundleLibraryClassLoader extends URLClassLoader {

	static {
		registerAsParallelCapable();
	}

	public BundleLibraryClassLoader(List<Path> jars, ClassLoader parent) throws MalformedURLException {
		super("FoundryEngine-BundleLibraries", toUrls(jars), parent);
	}

	private static URL[] toUrls(List<Path> jars) throws MalformedURLException {
		URL[] urls = new URL[jars.size()];
		for (int i = 0; i < jars.size(); i++) {
			urls[i] = jars.get(i).toUri().toURL();
		}
		return urls;
	}
}