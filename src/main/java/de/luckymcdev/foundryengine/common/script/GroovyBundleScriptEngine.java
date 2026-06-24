package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.script.event.GroovyScriptEngineModifyEvent;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import net.neoforged.fml.loading.FMLLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Groovy-based implementation of {@link BundleScriptEngine} for compiling bundle scripts.
 */
public class GroovyBundleScriptEngine implements BundleScriptEngine {
    @Nullable
    private GroovyScriptEngine engine;

    /**
     * Returns the file extension handled by this engine.
     */
    @Override
    public String fileExtension() {
        return ".groovy";
    }

    /**
     * Initializes the Groovy script engine with the bundle's script root.
     */
    @Override
    public void initialize(BundleFiles files) throws IOException {
        URL root = files.scripts().root().toUri().toURL();
        ClassLoader parent = FMLLoader.getCurrent().getCurrentClassLoader();

        engine = new GroovyScriptEngine(new URL[]{root}, parent);
        CompilerConfiguration config = buildConfig();

        Common.post(new GroovyScriptEngineModifyEvent(this, engine, config));

        engine.setConfig(config);
    }

    /**
     * Loads and compiles a Groovy script class by name.
     */
    @Override
    public Class<?> loadClass(String scriptName) throws NullPointerException, ResourceException, ScriptException {
        return engine.loadScriptByName(scriptName);
    }

    /**
     * Closes the Groovy script engine and releases resources.
     */
    @Override
    public void close() {
        engine = null;
    }

    private CompilerConfiguration buildConfig() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new ImportCustomizer());
        config.addCompilationCustomizers(buildSecurityCustomizer());
        return config;
    }

    private SecureASTCustomizer buildSecurityCustomizer() {
        SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setClosuresAllowed(true);
        secure.setMethodDefinitionAllowed(true);
        secure.setDisallowedImports(List.of(
                "java.io.*", "java.net.*", "javax.*", "sun.*", "com.sun.*", "jdk.*", "org.spongepowered.*"
        ));
        secure.setDisallowedReceivers(List.of("System", "Runtime", "Thread", "Class"));
        return secure;
    }
}