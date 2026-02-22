package io.github.luckymcdev.foundryengine.common.script;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.FoundryEngineMod;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class ScriptEngineFactory {
    private final Binding binding = new Binding();

    public Binding getGlobalBinding() {
        return binding;
    }

    public GroovyScriptEngine createScriptEngine(Path root) throws IOException {
        return createScriptEngine(root, FoundryEngineMod.class.getClassLoader());
    }

    public GroovyScriptEngine createScriptEngine(Path root, ClassLoader parent) throws IOException {
        return new GroovyScriptEngine(new URL[]{root.toUri().toURL()}, parent);
    }
}
