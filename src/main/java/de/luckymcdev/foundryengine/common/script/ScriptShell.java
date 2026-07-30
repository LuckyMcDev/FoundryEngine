package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.script.event.GroovyScriptEngineModifyEvent;
import groovy.lang.GroovyClassLoader;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

	public List<GroovyClass> compileBundle(List<Path> scriptPaths, Path scriptRoot) throws IOException {
		Path root = scriptRoot.normalize().toAbsolutePath();
		GroovyClassLoader loader = this.groovyClassLoader;
		if (scriptRoots.add(root)) {
			loader.addURL(root.toUri().toURL());
		}

		CompilationUnit unit = new CompilationUnit(compilerConfig, null, loader);
		for (Path scriptPath : scriptPaths) {
			unit.addSource(new File(scriptPath.toUri()));
		}
		unit.compile(Phases.CLASS_GENERATION);
		return unit.getClasses();
	}

	public void invalidateAll() {
		scriptRoots.clear();
		ClassLoader parent = new ScriptSandbox.FilteringClassLoader(
			FMLLoader.getCurrent().getCurrentClassLoader());
		this.groovyClassLoader = new GroovyClassLoader(parent, compilerConfig);
	}

	GroovyClassLoader getClassLoader() {
		return groovyClassLoader;
	}

	public CompilerConfiguration getCompilerConfiguration() {
		return compilerConfig;
	}
}
