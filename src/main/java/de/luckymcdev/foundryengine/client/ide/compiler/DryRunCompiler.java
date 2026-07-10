package de.luckymcdev.foundryengine.client.ide.compiler;

import de.luckymcdev.foundryengine.common.script.ScriptConfig;
import groovy.lang.GroovyClassLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.net.URL;

public final class DryRunCompiler {

	private DryRunCompiler() {
	}

	public static Int2ObjectMap<String> checkSyntax(String code, URL... scriptRoots) {
		Int2ObjectMap<String> errors = new Int2ObjectArrayMap<>();

		if (code == null || code.isBlank()) {
			return errors;
		}

		CompilerConfiguration config = ScriptConfig.createCompilerConfig();
		GroovyClassLoader gcl = new GroovyClassLoader(
			Thread.currentThread().getContextClassLoader(),
			config
		);
		for (URL root : scriptRoots) {
			if (root != null) {
				gcl.addURL(root);
			}
		}

		CompilationUnit cu = new CompilationUnit(config, null, gcl);
		cu.addSource("SyntaxCheck", code);

		try {
			cu.compile(Phases.SEMANTIC_ANALYSIS);
		} catch (MultipleCompilationErrorsException e) {
			for (var msg : e.getErrorCollector().getErrors()) {
				if (msg instanceof SyntaxErrorMessage sem) {
					SyntaxException se = sem.getCause();
					int line = se.getLine();
					String message = se.getMessage();
					errors.put(line > 0 ? line : 1, message);
				}
			}
		} catch (Exception e) {
			errors.put(1, e.getMessage() != null ? e.getMessage() : "Unknown error");
		}

		return errors;
	}
}