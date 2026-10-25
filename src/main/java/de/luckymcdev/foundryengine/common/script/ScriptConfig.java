package de.luckymcdev.foundryengine.common.script;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

import java.util.Collection;
import java.util.List;

/**
 * Shared CompilerConfiguration for all Groovy scripting in FoundryEngine.
 */
public final class ScriptConfig {

	private ScriptConfig() {
	}

	public static Collection<String> fileExtensions() {
		return List.of("groovy", "gvy");
	}

	public static CompilerConfiguration createCompilerConfig() {
		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(createImportCustomizer());
		config.addCompilationCustomizers(createSecureCustomizer());
		config.addCompilationCustomizers(LenientClosureTransformer.customizer());
		return config;
	}

	private static ImportCustomizer createImportCustomizer() {
		return new ImportCustomizer();
	}

	private static CompilationCustomizer createSecureCustomizer() {
		SecureASTCustomizer secure = new SecureASTCustomizer();

		secure.setClosuresAllowed(true);
		secure.setMethodDefinitionAllowed(true);
		secure.setIndirectImportCheckEnabled(true);

		// Blocklist for imports
		secure.setDisallowedImports(List.of(
			"java.io",
			"java.net",
			"java.lang.reflect",
			"java.lang.invoke",
			"java.util.prefs",
			"java.rmi",
			"javax.script",
			"sun",
			"com.sun",
			"jdk",
			"jdk.internal",
			"org.spongepowered",
			"org.objectweb.asm"
		));

		// Blocklist for receiver types
		secure.setDisallowedReceivers(List.of(
			"java.lang.Runtime",
			"java.lang.ProcessBuilder",
			"java.lang.Process",
			"java.lang.ProcessHandle",
			"java.lang.ClassLoader",
			"java.lang.Compiler",
			"java.lang.Thread",
			"java.lang.ThreadGroup",
			"java.lang.SecurityManager",
			"java.lang.System",
			"java.io.File",
			"java.io.InputStream",
			"java.io.OutputStream",
			"java.io.FileInputStream",
			"java.io.FileOutputStream",
			"java.io.FileReader",
			"java.io.FileWriter",
			"java.io.RandomAccessFile",
			"java.net.URL",
			"java.net.Socket",
			"java.net.ServerSocket",
			"java.net.HttpURLConnection",
			"java.nio.file.Files",
			"java.nio.file.Paths",
			"java.nio.file.FileSystem",
			"java.nio.file.FileSystems",
			"java.nio.file.Path",
			"java.rmi.Naming"
		));

		return new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
			@Override
			public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
				throws CompilationFailedException {
				if (ScriptSandbox.isFilterDisabled()) {
					return;
				}
				secure.call(source, context, classNode);
			}
		};
	}
}