package de.luckymcdev.foundryengine.common.game;

public enum GameLifecycle {
	STARTING, RUNNING, STOPPING, STOPPED;

	public boolean isStarting() {
		return this == STARTING;
	}

	public boolean isRunning() {
		return this == RUNNING;
	}

	public boolean isStopping() {
		return this == STOPPING;
	}

	public boolean isStopped() {
		return this == STOPPED;
	}
}
