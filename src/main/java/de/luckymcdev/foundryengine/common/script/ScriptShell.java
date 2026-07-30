package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.script.event.GroovyScriptEngineModifyEvent;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global Groovy script shell for compiling bundle scripts.
 * Shares a single {@link GroovyClassLoader} and {@link CompilerConfiguration}
 * across all bundles. Script roots are registered as URLs on the class loader
 * so that cross-script imports resolve correctly both within and between bundles.
 */
public final class ScriptShell {

	private final CompilerConfiguration compilerConfig;
	private final Set<Path> scriptRoots = ConcurrentHashMap.newKeySet();
	private volatile GroovyClassLoader groovyClassLoader;

	public ScriptShell() {
		ClassLoader parent = new ScriptSandbox.FilteringClassLoader(
			FMLLoader.getCurrent().getCurrentClassLoader());
		this.compilerConfig = ScriptConfig.createCompilerConfig();
		this.groovyClassLoader = new GroovyClassLoader(parent, compilerConfig);
		Common.post(new GroovyScriptEngineModifyEvent(this, compilerConfig));
	}

	public Class<?> compile(Path scriptPath, Path scriptRoot) throws Exception {
		Path root = scriptRoot.normalize().toAbsolutePath();
		GroovyClassLoader loader = this.groovyClassLoader;
		if (scriptRoots.add(root)) {
			loader.addURL(root.toUri().toURL());
		}
		return loader.parseClass(new GroovyCodeSource(scriptPath.toUri().toURL()));
	}

	public void invalidateAll() {
		scriptRoots.clear();
		ClassLoader parent = new ScriptSandbox.FilteringClassLoader(
			FMLLoader.getCurrent().getCurrentClassLoader());
		this.groovyClassLoader = new GroovyClassLoader(parent, compilerConfig);
	}

	public CompilerConfiguration getCompilerConfiguration() {
		return compilerConfig;
	}
}
