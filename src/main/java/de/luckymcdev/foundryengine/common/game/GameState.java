package de.luckymcdev.foundryengine.common.game;

/**
 * Lifecycle states for a game session.
 */
public enum GameState {
	STARTING, RUNNING, STOPPING, STOPPED;

	/**
	 * Returns true if the state is STARTING.
	 */
	public boolean isStarting() {
		return this == STARTING;
	}

	/**
	 * Returns true if the state is RUNNING.
	 */
	public boolean isRunning() {
		return this == RUNNING;
	}

	/**
	 * Returns true if the state is STOPPING.
	 */
	public boolean isStopping() {
		return this == STOPPING;
	}

	/**
	 * Returns true if the state is STOPPED.
	 */
	public boolean isStopped() {
		return this == STOPPED;
	}
}
