package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.script.event.GroovyScriptEngineModifyEvent;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;

/**
 * Groovy-based script engine for compiling bundle scripts.
 */
public class GroovyBundleScriptEngine {
	@Nullable
	private GroovyScriptEngine engine;

	public void initialize(BundleFiles files) throws IOException {
		URL root = files.scripts().root().toUri().toURL();
		ClassLoader parent = FMLLoader.getCurrent().getCurrentClassLoader();
		ClassLoader sandboxed = new ScriptSandbox.FilteringClassLoader(parent);

		engine = new GroovyScriptEngine(new URL[]{root}, sandboxed);
		CompilerConfiguration config = ScriptConfig.createCompilerConfig();

		Common.post(new GroovyScriptEngineModifyEvent(this, engine, config));

		engine.setConfig(config);
	}

	public Class<?> loadClass(String scriptName) throws NullPointerException, ResourceException, ScriptException {
		return engine.loadScriptByName(scriptName);
	}

	public void close() {
		engine = null;
	}
}