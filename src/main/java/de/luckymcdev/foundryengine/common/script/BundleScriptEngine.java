package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;

import java.io.IOException;

public interface BundleScriptEngine {

    String fileExtension();

    void initialize(BundleFiles files) throws IOException;

    Class<?> loadClass(String scriptName) throws Exception;

    void close();
}