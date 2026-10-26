package de.luckymcdev.foundryengine.common.script.runtime;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.script.ScriptConfig;
import de.luckymcdev.foundryengine.common.script.definition.ScriptDefinition;
import de.luckymcdev.foundryengine.common.script.definition.ScriptSource;
import de.luckymcdev.foundryengine.common.script.event.GroovyScriptEngineModifyEvent;
import de.luckymcdev.foundryengine.common.script.sandbox.FilteringClassLoader;
import de.luckymcdev.foundryengine.common.script.sandbox.SandboxChecker;
import de.luckymcdev.foundryengine.common.script.sandbox.SandboxPolicy;
import groovy.lang.GroovyClassLoader;
import groovy.transform.ThreadInterrupt;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.tools.GroovyClass;
import org.slf4j.Logger;

import java.io.IOException;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe bundle compiler. Every call to {@link #compile} builds its own configuration,
 * class loader, and compilation unit, so bundles can be compiled in parallel.
 * <p>
 * Sandbox customizers are added here, after the {@link GroovyScriptEngineModifyEvent}, so
 * a mod cannot remove them. With {@code policy.enabled() == false} none of them is added and
 * the parent class loader is not filtered.
 */
public class ScriptCompiler {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final CompilerConfiguration template;
	private final List<CompilationCustomizer> baseCustomizers;
	private final ClassLoader hostLoader;

	public ScriptCompiler() {
		this.hostLoader = FMLLoader.getCurrent().getCurrentClassLoader();
		this.template = ScriptConfig.createBaseCompilerConfig();
		Common.post(new GroovyScriptEngineModifyEvent(template));
		this.baseCustomizers = List.copyOf(template.getCompilationCustomizers());
	}

	private static void logErrors(ScriptSource source, MultipleCompilationErrorsException mce) {
		String file = source != null ? source.className() : "<batch>";
		for (var message : mce.getErrorCollector().getErrors()) {
			if (message instanceof SyntaxErrorMessage syntax) {
				LOGGER.warn("Script error in '{}' ({},{}): {}", file,
					syntax.getCause().getLine(), syntax.getCause().getStartColumn(), syntax.getCause().getMessage());
			} else {
				LOGGER.warn("Script error in '{}': {}", file, message);
			}
		}
	}

	public CompiledBundle compile(ScriptDefinition definition, SandboxPolicy policy) {
		String bundleId = definition.bundleId();
		int scriptCount = definition.sources().size();
		CompilerConfiguration config = configure(policy);
		ClassLoader parent = policy.enabled() ? new FilteringClassLoader(hostLoader) : hostLoader;
		GroovyClassLoader loader = new GroovyClassLoader(parent, config);

		Map<String, Class<?>> classes = new LinkedHashMap<>();
		List<ScriptFailure> failures = new ArrayList<>();


		// May want to re-enable this, since its a cool ass feature but also kinda a security risk
		if (policy.enabled()) {
			System.setProperty("groovy.grape.enable", "false");
		}

		long start = System.nanoTime();
		if (!definition.sources().isEmpty()) {
			LOGGER.info("Compiling {} scripts for bundle '{}'", scriptCount, bundleId);
			try {
				loader.addURL(definition.root().toUri().toURL());
				List<GroovyClass> compiled = compileSources(definition, definition.sources(), config, loader);
				define(loader, compiled, classes, failures, bundleId);
			} catch (MultipleCompilationErrorsException mce) {
				LOGGER.warn("Batch compile failed for bundle '{}', falling back to per-file compilation", bundleId);
				logErrors(null, mce);
				compilePerFile(definition, config, loader, classes, failures);
			} catch (Exception | LinkageError e) {
				LOGGER.error("Batch compile failed unexpectedly for bundle '{}'", bundleId, e);
				compilePerFile(definition, config, loader, classes, failures);
			}
			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			if (failures.isEmpty()) {
				LOGGER.info("Compiled {} scripts for bundle '{}' in {} ms",
					scriptCount, bundleId, elapsedMs);
			} else {
				LOGGER.warn("Compiled {} scripts for bundle '{}' in {} ms ({} failures)",
					scriptCount, bundleId, elapsedMs, failures.size());
			}
		}
		return new CompiledBundle(loader, classes, failures);
	}

	private CompilerConfiguration configure(SandboxPolicy policy) {
		CompilerConfiguration config = new CompilerConfiguration(template);
		List<CompilationCustomizer> ordered = new ArrayList<>();
		if (policy.enabled()) {
			ordered.add(SandboxChecker.preResolve());
		}
		ordered.addAll(baseCustomizers);
		if (policy.enabled()) {
			ordered.add(SandboxChecker.postResolve());
			// Must come after the checker: it generates calls to java.lang.Thread.
			ordered.add(new ASTTransformationCustomizer(ThreadInterrupt.class));
		}
		config.getCompilationCustomizers().clear();
		config.addCompilationCustomizers(ordered.toArray(new CompilationCustomizer[0]));
		return config;
	}

	private List<GroovyClass> compileSources(ScriptDefinition definition, List<ScriptSource> sources,
											 CompilerConfiguration config, GroovyClassLoader loader) throws IOException {
		CodeSource codeSource = new CodeSource(definition.root().toUri().toURL(), (Certificate[]) null);
		CompilationUnit unit = new CompilationUnit(config, codeSource, loader);
		for (ScriptSource source : sources) {
			unit.addSource(source.path().toFile());
		}
		unit.compile(Phases.CLASS_GENERATION);
		return unit.getClasses();
	}

	private void compilePerFile(ScriptDefinition definition, CompilerConfiguration config, GroovyClassLoader loader,
								Map<String, Class<?>> classes, List<ScriptFailure> failures) {
		for (ScriptSource source : definition.sources()) {
			try {
				List<GroovyClass> compiled = compileSources(definition, List.of(source), config, loader);
				define(loader, compiled, classes, failures, definition.bundleId());
			} catch (MultipleCompilationErrorsException mce) {
				logErrors(source, mce);
				failures.add(new ScriptFailure(source.className(), "compile", mce));
			} catch (Exception | LinkageError e) {
				LOGGER.warn("Failed to compile script '{}' for bundle '{}'", source.className(), definition.bundleId(), e);
				failures.add(new ScriptFailure(source.className(), "compile", e));
			}
		}
	}

	/**
	 * Defines in compile order (superclasses first); skips classes an earlier unit already defined.
	 */
	private void define(GroovyClassLoader loader, List<GroovyClass> compiled, Map<String, Class<?>> classes,
						List<ScriptFailure> failures, String bundleId) {
		for (GroovyClass groovyClass : compiled) {
			String name = groovyClass.getName();
			if (classes.containsKey(name)) {
				continue;
			}
			try {
				classes.put(name, loader.defineClass(name, groovyClass.getBytes()));
			} catch (Exception | LinkageError e) {
				LOGGER.warn("Failed to define class '{}' in bundle '{}'", name, bundleId, e);
				failures.add(new ScriptFailure(name, "define", e));
			}
		}
	}
}