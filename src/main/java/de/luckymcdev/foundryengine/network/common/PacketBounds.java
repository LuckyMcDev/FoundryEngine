package de.luckymcdev.foundryengine.network.common;

public enum PacketBounds {
	/**
	 * Server -> Client
	 */
	CLIENT,
	/**
	 * Client -> Server
	 */
	SERVER,
	/**
	 * Client -> Server
	 * AND
	 * Server -> Client
	 */
	BOTH;

	public boolean isBoth() {
		return this == BOTH;
	}

	public boolean isServer() {
		return this == SERVER;
	}

	public boolean isClient() {
		return this == CLIENT;
	}
}
