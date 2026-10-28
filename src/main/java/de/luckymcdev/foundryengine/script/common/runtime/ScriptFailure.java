package de.luckymcdev.foundryengine.script.common.runtime;

import org.jspecify.annotations.Nullable;

/**
 * One thing that went wrong in a bundle.
 *
 * @param script the script or class involved, null for bundle-wide failures
 * @param stage  compile, define, instantiate, onLoad or onUnload
 * @param cause  the exception (a ScriptTimeoutException for timeouts)
 */
public record ScriptFailure(@Nullable String script, String stage, Throwable cause) {
}