package de.luckymcdev.foundryengine.common.script.definition;

import java.nio.file.Path;

/**
 * One script file of a bundle.
 *
 * @param className the file name without extension, also the expected entrypoint class name
 * @param path      the real (symlink-resolved) path, guaranteed to be inside the bundle's script root
 * @param env       the environment directory the file lives in
 */
public record ScriptSource(String className, Path path, ScriptEnv env) {
}