package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import groovy.util.GroovyScriptEngine;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Factory for creating and configuring GroovyScriptEngine instances.
 */
public class ScriptEngineFactory {
    private static final Logger LOGGER = LogUtils.getLogger();

    public GroovyScriptEngine create(BundleFiles files) throws IOException {
        URL[] roots = buildRoots(files);
        ClassLoader parentClassLoader = FMLLoader.getCurrent().getCurrentClassLoader();

        GroovyScriptEngine engine = new GroovyScriptEngine(roots, parentClassLoader);
        CompilerConfiguration config = createCompilerConfiguration();

        Common.post(new ScriptEngineModifyEvent(engine, config));

        engine.setConfig(config);

        return engine;
    }

    private URL[] buildRoots(BundleFiles files) throws IOException {
        return new URL[]{files.root().toUri().toURL()};
    }

    public CompilerConfiguration createCompilerConfiguration() {
        CompilerConfiguration config = new CompilerConfiguration();

        ImportCustomizer importCustomizer = new ImportCustomizer();
        config.addCompilationCustomizers(importCustomizer);

        SecureASTCustomizer secure = createSecurityCustomizer();
        config.addCompilationCustomizers(secure);

        return config;
    }

    private SecureASTCustomizer createSecurityCustomizer() {
        SecureASTCustomizer secure = new SecureASTCustomizer();

        secure.setClosuresAllowed(true);
        secure.setMethodDefinitionAllowed(true);

        // Disallow dangerous imports
        secure.setDisallowedImports(List.of(
                "java.io.*",
                "java.net.*",
                "javax.*",
                "sun.*",
                "com.sun.*",
                "jdk.*"
        ));

        // Disallow dangerous receivers
        secure.setDisallowedReceivers(List.of(
                "System",
                "Runtime",
                "Thread",
                "Class"
        ));

        return secure;
    }
}