package de.luckymcdev.foundryengine.common.script.sandbox;

import de.luckymcdev.foundryengine.common.Common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The user's consent to turn the sandbox off. Backed by a marker file
 * (same location as before, existing consents stay valid).
 */
public final class SandboxWaiver {

	private static final Path FILE = Common.USER_SPACE.resolve("sandbox_waiver.lock");
	private static volatile boolean accepted = Files.exists(FILE);

	private SandboxWaiver() {
	}

	public static boolean isAccepted() {
		return accepted;
	}

	public static void markAccepted() throws IOException {
		Files.createDirectories(FILE.getParent());
		if (Files.notExists(FILE)) {
			Files.createFile(FILE);
		}
		accepted = true;
	}

	/**
	 * Takes effect immediately for bundles compiled afterwards.
	 */
	public static void revoke() throws IOException {
		accepted = false;
		Files.deleteIfExists(FILE);
	}
}