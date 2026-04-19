package de.luckymcdev.foundryengine.common.script.event;

import de.luckymcdev.foundryengine.common.script.GroovyBundleScriptEngine;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Event posted when the Groovy script engine is being initialized.
 * Allows modification of the {@link GroovyScriptEngine} and its {@link CompilerConfiguration}.
 */
public class GroovyScriptEngineModifyEvent extends ScriptEngineModifyEvent {

    private final GroovyScriptEngine groovyEngine;
    private final CompilerConfiguration compilerConfiguration;

    public GroovyScriptEngineModifyEvent(GroovyBundleScriptEngine bundleEngine,
                                         GroovyScriptEngine groovyEngine,
                                         CompilerConfiguration compilerConfiguration) {
        super(bundleEngine);
        this.groovyEngine = groovyEngine;
        this.compilerConfiguration = compilerConfiguration;
    }

    public GroovyScriptEngine getGroovyScriptEngine() {
        return groovyEngine;
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }
}