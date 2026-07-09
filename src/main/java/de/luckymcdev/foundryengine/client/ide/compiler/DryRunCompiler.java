package de.luckymcdev.foundryengine.client.ide.compiler;

import de.luckymcdev.foundryengine.common.script.ScriptConfig;
import groovy.lang.GroovyShell;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

public final class DryRunCompiler {

	private DryRunCompiler() {
	}

	public static Int2ObjectMap<String> checkSyntax(String code) {
		Int2ObjectMap<String> errors = new Int2ObjectArrayMap<>();

		if (code == null || code.isBlank()) {
			return errors;
		}

		GroovyShell shell = new GroovyShell(
			Thread.currentThread().getContextClassLoader(),
			ScriptConfig.createCompilerConfig()
		);

		try {
			shell.parse(code);
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