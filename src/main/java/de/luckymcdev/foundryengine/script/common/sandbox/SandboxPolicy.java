package de.luckymcdev.foundryengine.script.common.sandbox;

import de.luckymcdev.foundryengine.config.StartupConfig;

/**
 * Immutable snapshot of the sandbox settings, taken when a bundle is defined.
 * <p>
 * {@code enabled == false} turns off ALL sandboxing: no class loader filter, no AST checks,
 * no interrupt checks and no timeouts. It requires the config option AND the user's consent.
 *
 * @param enabled        whether sandbox restrictions apply
 * @param timeoutSeconds max seconds for script entry code, 0 = unlimited
 */
public record SandboxPolicy(boolean enabled, long timeoutSeconds) {

	public static SandboxPolicy current() {
		boolean disabled = StartupConfig.DISABLE_CLASS_FILTER.get() && SandboxWaiver.isAccepted();
		if (disabled) {
			return new SandboxPolicy(false, 0);
		}
		return new SandboxPolicy(true, StartupConfig.SCRIPT_TIMEOUT_SECONDS.get());
	}

	public boolean hasTimeout() {
		return timeoutSeconds > 0;
	}
}