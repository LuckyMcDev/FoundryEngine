package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.script.event.GroovyScriptEngineModifyEvent;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global Groovy script shell for compiling bundle scripts.
 * Shares a single {@link GroovyClassLoader} and {@link CompilerConfiguration}
 * across all bundles. Script roots are registered as URLs on the class loader
 * so that cross-script imports resolve correctly both within and between bundles.
 */
public final class ScriptShell {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final CompilerConfiguration compilerConfig;
	private final Set<Path> scriptRoots = ConcurrentHashMap.newKeySet();
	private final Object loaderLock = new Object();
	private volatile GroovyClassLoader groovyClassLoader;

	public ScriptShell() {
		ClassLoader parent = new ScriptSandbox.FilteringClassLoader(
			FMLLoader.getCurrent().getCurrentClassLoader());
		this.compilerConfig = ScriptConfig.createCompilerConfig();
		Common.post(new GroovyScriptEngineModifyEvent(this, compilerConfig));
		this.groovyClassLoader = new GroovyClassLoader(parent, compilerConfig);
	}

	private static void closeQuietly(GroovyClassLoader loader) {
		if (loader != null) {
			try {
				loader.close();
			} catch (IOException e) {
				LOGGER.warn("Failed to close invalidated Groovy script class loader", e);
			}
		}
	}

	public List<GroovyClass> compileBundle(List<Path> scriptPaths, Path scriptRoot) throws IOException {
		Path root = scriptRoot.normalize().toAbsolutePath();
		GroovyClassLoader loader;
		synchronized (loaderLock) {
			loader = this.groovyClassLoader;
			if (scriptRoots.add(root)) {
				loader.addURL(root.toUri().toURL());
			}
		}

		CodeSource codeSource = new CodeSource(root.toUri().toURL(), (Certificate[]) null);
		CompilationUnit unit = new CompilationUnit(compilerConfig, codeSource, loader);
		for (Path scriptPath : scriptPaths) {
			unit.addSource(new File(scriptPath.toUri()));
		}
		unit.compile(Phases.CLASS_GENERATION);
		return unit.getClasses();
	}

	public void invalidateAll() {
		synchronized (loaderLock) {
			scriptRoots.clear();
			GroovyClassLoader old = this.groovyClassLoader;
			ClassLoader parent = new ScriptSandbox.FilteringClassLoader(
				FMLLoader.getCurrent().getCurrentClassLoader());
			this.groovyClassLoader = new GroovyClassLoader(parent, compilerConfig);
			closeQuietly(old);
		}
	}

	Class<?> defineClass(String name, byte[] bytes) {
		synchronized (loaderLock) {
			return groovyClassLoader.defineClass(name, bytes);
		}
	}

	Class<?> parseClass(GroovyCodeSource codeSource) {
		synchronized (loaderLock) {
			return groovyClassLoader.parseClass(codeSource);
		}
	}

	public CompilerConfiguration getCompilerConfiguration() {
		return compilerConfig;
	}
}