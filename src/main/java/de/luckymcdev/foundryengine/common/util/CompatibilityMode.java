package de.luckymcdev.foundryengine.common.util;

public enum CompatibilityMode {
	BOTH("both"),
	CLIENT("client"),
	SERVER("server");

	private final String name;

	CompatibilityMode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Whether this mode covers the client side, allowing content that only needs to exist on
	 * the client (e.g. sounds, particles) to be registered.
	 */
	public boolean supportsClient() {
		return this == BOTH || this == CLIENT;
	}

	/**
	 * Whether this mode covers the server side, allowing content that only needs to exist on
	 * the server (e.g. recipes) to be registered.
	 */
	public boolean supportsServer() {
		return this == BOTH || this == SERVER;
	}

	/**
	 * Whether this mode covers both the client and the server, allowing content that must exist
	 * on both sides (e.g. items, blocks, block entities, tags) to be registered.
	 */
	public boolean requiresBothSides() {
		return this == BOTH;
	}
}
