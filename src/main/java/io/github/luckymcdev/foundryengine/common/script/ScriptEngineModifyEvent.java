package io.github.luckymcdev.foundryengine.common.script;

import groovy.util.GroovyScriptEngine;
import net.neoforged.bus.api.Event;
import org.codehaus.groovy.control.CompilerConfiguration;

public class ScriptEngineModifyEvent extends Event {
    private final GroovyScriptEngine scriptEngine;
    private final CompilerConfiguration compilerConfiguration;

    public ScriptEngineModifyEvent(GroovyScriptEngine scriptEngine, CompilerConfiguration compilerConfiguration) {
        this.scriptEngine = scriptEngine;
        this.compilerConfiguration = compilerConfiguration;
    }

    public GroovyScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }
}
